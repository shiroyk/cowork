use crate::config::{Config, StreamConfig};
use crate::model::{CollabMessage, Doc, DocVector, Event, COLL_DOC_NAME, COLL_VECTOR_NAME};
use crate::service::load_doc;
use async_nats::jetstream::consumer::pull::BatchError;
use async_nats::jetstream::consumer::{AckPolicy, Consumer};
use async_nats::jetstream::stream::Config as JetStreamConfig;
use async_nats::jetstream::stream::RetentionPolicy::Interest;
use async_nats::{jetstream, Client};
use bytes::Bytes;
use jetstream::consumer::pull::Config as ConsumerConfig;
use mongodb::bson::doc;
use mongodb::Database;
use std::collections::{HashMap, HashSet};
use std::env;
use std::time::SystemTime;
use tokio::spawn;
use tokio::time::sleep;
use tonic::codegen::tokio_stream::StreamExt;
use yrs::{Doc as YDoc, Transact, Update};
use yrs::updates::decoder::Decode;
use log::debug;

#[derive(Debug, Clone)]
pub struct EventStream {
    db: Database,
    client: Client,
    consumer: Consumer<ConsumerConfig>,
    cfg: StreamConfig,
}

impl EventStream {
    pub async fn new(cfg: Config, db: Database) -> Self {
        let consumer_name = format!("persistence-{}", if common::is_dev() { "doc-dev".to_string() } else {
            env::var("HOSTNAME").expect("HOSTNAME is not set")
        });

        let client = async_nats::connect(cfg.nats).await
            .expect("failed to connect to nats");

        // create stream
        let stream = jetstream::new(client.clone())
            .get_or_create_stream(JetStreamConfig {
                name: "stream_collab_persistence".to_string(),
                subjects: vec![Event::Update.subject()],
                retention: Interest,
                max_age: cfg.stream.max_age,
                ..Default::default()
            })
            .await
            .expect("failed to create stream");

        // create consumer
        let consumer = stream
            .get_or_create_consumer(
                consumer_name.clone().as_str(), ConsumerConfig {
                    durable_name: Some(consumer_name.into()),
                    filter_subject: Event::Update.subject(),
                    ack_policy: AckPolicy::Explicit,
                    ..Default::default()
                },
            )
            .await.expect("failed to create consumer");

        EventStream {
            db,
            client,
            consumer,
            cfg: cfg.stream,
        }
    }

    pub fn start(self) {
        println!("=> doc event consumer start");
        spawn(async move {
            let mut buffer = Vec::<DocVector>::with_capacity(self.cfg.batch);
            loop {
                // fetch messages
                if let Err(_) = self.fetch(&mut buffer).await {
                    continue;
                }

                // wait more messages
                if buffer.len() < buffer.capacity()  {
                    sleep(self.cfg.wait).await;
                    if buffer.len() == 0 { continue }
                }

                // persist messages
                let self_clone = self.clone();
                let batch = buffer.clone();
                buffer.clear();
                spawn(async move { self_clone.persist(batch).await });
            }
        });
    }

    async fn fetch(&self, buffers: &mut Vec<DocVector>) -> Result<(), BatchError> {
        let mut messages = self.consumer.fetch()
            .max_messages(self.cfg.batch)
            .expires(self.cfg.wait)
            .messages().await?;

        // deserialize messages
        while let Some(message) = messages.next().await {
            if message.is_err() {
                continue;
            }
            let msg = message.unwrap();
            let collab_message = match rmp_serde::from_slice
                ::<CollabMessage>(msg.payload.as_ref()) {
                Ok(x) => x,
                Err(_) => continue,
            };

            if collab_message.data.is_empty() {
                continue;
            }
            buffers.push(DocVector {
                did: collab_message.did,
                uid: collab_message.uid,
                data: collab_message.data,
            });
            let _ = msg.ack().await;
        }
        Ok(())
    }

    async fn persist(&self, vectors: Vec<DocVector>) -> Result<(), mongodb::error::Error> {
        debug!("{}: => doc persistence start {}", SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs(), vectors.len());
        let mut saved = HashMap::<String, HashSet<String>>::new();
        for x in &vectors {
            if let Some(set) = saved.get_mut(&x.did) {
                set.insert(x.uid.clone());
            } else {
                saved.insert(x.did.clone(), HashSet::from([x.uid.clone()]));
            }
        }

        // persistence doc updates
        self.db.collection(COLL_VECTOR_NAME).insert_many(vectors).await?;

        let subject = Event::Save.subject();
        let now = SystemTime::now().duration_since(SystemTime::UNIX_EPOCH).unwrap().as_secs() as i64;
        let now_data = rmp_serde::to_vec(&now).unwrap();

        let coll_doc = self.db.collection::<Doc>(COLL_DOC_NAME);
        let save_message = CollabMessage {
            event: Event::Save,
            uid: "".to_string(),
            did: "".to_string(),
            data: Bytes::from(now_data),
        };

        // update last_updated and publish save events
        for (did, uids) in saved {
            let result = match coll_doc.update_one(
                doc! { "did": did.clone() },
                doc! { "$set": { "lastUpdated": now } })
                .await {
                Ok(result) => result,
                Err(_) => continue
            };
            if result.modified_count == 0 {
                continue;
            }

            for uid in uids {
                let mut msg = save_message.clone();
                msg.uid = uid;
                msg.did = did.clone();
                let data = match rmp_serde::to_vec_named(&msg) {
                    Ok(data) => data,
                    Err(_) => continue
                };
                let _ = self.client.publish(subject.clone(), Bytes::from(data)).await;
            }
        }
        let _ = self.client.flush().await;
        Ok(())
    }

    // todo: remote doc
    #[allow(dead_code)]
    async fn load_remote_doc(&self, did: String) {
        let content = match load_doc(&self.db, did).await {
            Ok(x) => x,
            Err(_) => return
        };
        if let None = content { return }

        let content = content.unwrap();
        let doc = YDoc::new();
        let mut txn = doc.transact_mut();
        let vector  = match Update::decode_v2(&*content.data) {
            Ok(x) => x,
            Err(_) => return
        };
        if let Err(_) = txn.apply_update(vector) {
            return ;
        }
    }
}