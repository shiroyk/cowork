use std::time::Instant;
use crate::model::{Doc, DocContent, DocQuery, DocVector, COLL_CONTENT_NAME, COLL_DOC_NAME, COLL_VECTOR_NAME, DB_NAME};
use mongodb::bson::{doc, Document};
use mongodb::error::Error;
use mongodb::{Client, Database, IndexModel};
use mongodb::bson::oid::ObjectId;
use mongodb::options::IndexOptions;
use tonic::codegen::tokio_stream::StreamExt;

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

pub async fn create(db: &Database, mut doc: Doc) -> Result<String, Error> {
    let collection = db.collection(COLL_DOC_NAME);
    if doc.did.is_empty() {
        doc.did = ObjectId::new().to_string();
    }
    doc.created_at = Instant::now().elapsed().as_secs() as i64;
    doc.updated_at = doc.created_at;
    let did = doc.did.clone();
    collection.insert_one(doc).await?;
    Ok(did)
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

pub async fn load_doc(db: &Database, id: String) -> Result<Option<DocContent>, Error> {
    let result = db.collection::<DocContent>(COLL_CONTENT_NAME).find_one(doc! { "did": id }).await?;
    Ok(result)
}