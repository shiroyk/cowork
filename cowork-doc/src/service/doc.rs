use std::collections::HashSet;
use std::str::FromStr;
use std::time::{Instant, SystemTime};
use bytes::Bytes;
use log::debug;
use crate::model::{Doc, DocContent, DocQuery, DocVector, COLL_CONTENT_NAME, COLL_DOC_NAME, COLL_VECTOR_NAME, DB_NAME};
use mongodb::bson::{doc, Binary, Document};
use mongodb::error::Error;
use mongodb::{bson, Client, Database, IndexModel};
use mongodb::bson::oid::ObjectId;
use mongodb::bson::spec::BinarySubtype;
use mongodb::options::IndexOptions;
use tonic::codegen::tokio_stream::StreamExt;
use yrs::{Doc as YDoc, Transact, Update};
use yrs::updates::decoder::Decode;

pub async fn create_index(client: &Client) {
    let database = client.database(DB_NAME);
    let doc_model = IndexModel::builder()
        .keys(doc! { "uid": 1 })
        .build();
    let doc_model_unique = IndexModel::builder()
        .keys(doc! { "did": 1 })
        .options(IndexOptions::builder().unique(true).build())
        .build();
    database
        .collection::<Doc>(COLL_DOC_NAME)
        .create_indexes(vec![doc_model, doc_model_unique])
        .await
        .expect("Failed to create index");
    database
        .collection::<DocVector>(COLL_VECTOR_NAME)
        .create_index(IndexModel::builder()
            .keys(doc! { "did": -1, "uid": 1 })
            .build())
        .await
        .expect("Failed to create index");
}

pub async fn search(db: &Database, query: DocQuery) -> Result<Vec<Doc>, Error> {
    let collection = db.collection::<Doc>(COLL_DOC_NAME);
    let mut filter = Document::new();
    if let Some(title) = query.title {
        filter.insert("title", doc! { "$regex": title, "$options": "i" });
    }
    if let Some(trash) = query.trash {
        filter.insert("trash", trash);
    }
    let result = collection.find(filter)
        .limit(query.limit.unwrap_or(10))
        .skip(query.offset.unwrap_or(0))
        .await?;
    let x = result.map(|x| x.unwrap()).collect::<Vec<Doc>>().await;
    Ok(x)
}

pub async fn find_by_id(db: &Database, id: String) -> Result<Option<Doc>, Error> {
    let collection = db.collection(COLL_DOC_NAME);
    let result = collection.find_one(doc! { "id": id }).await?;
    Ok(result)
}

pub async fn create(db: &Database, mut doc: Doc) -> Result<Doc, Error> {
    let collection = db.collection(COLL_DOC_NAME);
    if doc.did.is_empty() {
        doc.did = ObjectId::new().to_string();
    }
    doc.created_at = Instant::now().elapsed().as_secs() as i64;
    doc.updated_at = doc.created_at;
    let doc_ref = doc.clone();
    collection.insert_one(doc).await?;
    Ok(doc_ref)
}

pub async fn update(db: &Database, doc: Doc) -> Result<(), Error> {
    let collection = db.collection::<Doc>(COLL_DOC_NAME);
    let update = doc! {
            "$set": doc! {
                "title": &doc.title,
                "trash": doc.trash,
                "updatedAt": Instant::now().elapsed().as_secs() as i64
            }
        };
    let _ = collection.update_one(doc! { "did": &doc.did }, update).await?;
    Ok(())
}

pub async fn delete(db: &Database, id: String) -> Result<(), Error> {
    let mut session = db.client().start_session().await?;
    // start a transaction
    session.start_transaction().and_run(id, |session, id| Box::pin(async {
        let db = session.client().database(DB_NAME);
        let result = db.collection::<Doc>(COLL_DOC_NAME).delete_one(doc! { "id": id.clone() }).await?;
        if result.deleted_count > 0 {
            db.collection::<DocVector>(COLL_VECTOR_NAME).delete_many(doc! { "did": id.clone() }).await?;
            db.collection::<DocContent>(COLL_CONTENT_NAME).delete_many(doc! { "did": id }).await?;
        }
        Ok(())
    })).await?;
    Ok(())
}

pub async fn doc_vector(db: &Database, did: String) -> Result<Vec<DocVector>, Error> {
    let coll = db.collection::<DocVector>(COLL_VECTOR_NAME);
    let result = coll.find(doc! { "did": did }).await?;
    let x = result.map(|x| x.unwrap()).collect::<Vec<DocVector>>().await;
    Ok(x)
}

/// flush content and return the content
pub async fn flush_content(db: &Database, did: String) -> Result<Vec<u8>, Error> {
    let mut session = db.client().start_session().await?;
    session.start_transaction().await?;

    // find the latest vector
    let latest = db.collection::<Document>(COLL_VECTOR_NAME)
        .find_one(doc! { "did": did.clone() })
        .projection(doc! { "_id": 1 }).session(&mut session).sort(doc! { "_id": -1 })
        .await?;
    let vector = if let Some(mut v) = latest {
        v.get_object_id_mut("_id").unwrap().to_string()
    } else {
        "".to_string()
    };

    // load the content
    let coll = db.collection::<DocContent>(COLL_CONTENT_NAME);
    let mut content = if let Some(x) = coll
        .find_one(doc! { "did": did.clone() }).session(&mut session).await? {
        x
    } else {
        DocContent { id: None, vector: "".to_string(), wait_flush: 0, did: did.clone(), data: Bytes::new() }
    };

    if content.vector == vector {
        return Ok(content.data.to_vec());
    }

    // load the vector
    let mut query = doc! { "did": did.clone() };
    if !content.vector.is_empty() {
        query.insert("_id", doc! { "$gt": ObjectId::from_str(&content.vector).unwrap() });
    }
    let mut vectors = db.collection::<DocVector>(COLL_VECTOR_NAME)
        .find(query).session(&mut session).await?;
    let vec = vectors.stream(&mut session)
        .map(|x| x.unwrap()).collect::<Vec<DocVector>>().await;
    let mut flushed = 0;

    {
        let doc = YDoc::new();
        let mut txn = doc.transact_mut();
        if let Ok(x) = Update::decode_v2(&*content.data) {
            let _ = txn.apply_update(x);
        }

        // apply the updates
        for v in vec {
            if let Ok(x) = Update::decode_v2(&*v.data.to_vec()) {
                let _ = txn.apply_update(x);
                flushed += 1;
            }
        }
        txn.commit();

        content.data = Bytes::from(txn.encode_update_v2());
    }

    let data = content.data.to_vec();
    content.vector = vector.clone();
    // save the content
    if content.id.is_none() {
        content.id = Some(ObjectId::new().to_string());
        coll.insert_one(content).session(&mut session).await?;
    } else {
        let data = Binary { subtype: BinarySubtype::Generic, bytes: data.clone() };
        let update = doc! { "$set": { "data": data, "vector": vector }, "$inc": { "waitFlush": -flushed } };
        coll.update_one(doc! { "did": did.clone() }, update)
            .session(&mut session).await?;
        session.commit_transaction().await?;
    }

    debug!("{}: => doc flush vectors {}", SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs(), flushed);

    session.commit_transaction().await?;

    Ok(data)
}

pub async fn get_doc_client_id(db : &Database, did: String, uid: String) -> Result<Option<i32>, Error> {
    let mut session = db.client().start_session().await?;
    session.start_transaction().await?;
    let coll = db.collection::<Doc>(COLL_DOC_NAME);

    let doc = match coll.find_one(doc! { "did": did }).await? {
        Some(x) => x,
        None => return Ok(None),
    };

    if let Some(v) = doc.clients.get(&uid) {
        return Ok(Some(v.clone()));
    }
    let clients = doc.clients.values().cloned().collect::<HashSet<i32>>();

    let mut rng = fastrand::Rng::new();
    let mut new_id = rng.i32(0..i32::MAX);

    while clients.contains(&new_id) {
        new_id = rng.i32(0..i32::MAX);
    }
    let mut doc_clients = doc.clients.clone();
    doc_clients.insert(uid, new_id);
    let data = bson::to_document(&doc_clients)?;
    coll.update_one(doc! { "did": doc.did }, doc! { "$set": { "clients": data } })
        .session(&mut session).await?;

    session.commit_transaction().await?;

    Ok(Some(new_id))
}