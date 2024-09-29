use crate::config::{Config, StreamConfig};
use crate::model::{CollabMessage, Doc, DocVector, Event, COLL_CONTENT_NAME, COLL_DOC_NAME, COLL_VECTOR_NAME};
use async_nats::jetstream::consumer::pull::BatchError;
use async_nats::jetstream::consumer::{AckPolicy, Consumer};
use async_nats::jetstream::stream::Config as JetStreamConfig;
use async_nats::jetstream::stream::RetentionPolicy::Interest;
use async_nats::{jetstream, Client};
use bytes::Bytes;
use jetstream::consumer::pull::Config as ConsumerConfig;
use mongodb::bson::{doc, Document};
use mongodb::Database;
use std::collections::{HashMap, HashSet};
use std::env;
use std::time::SystemTime;
use tokio::spawn;
use tokio::time::sleep;
use tonic::codegen::tokio_stream::StreamExt;
use log::{debug, error};
use crate::service::flush_content;

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
                if let Err(err) = self.fetch(&mut buffer).await {
                    error!("failed to fetch messages: {}", err);
                    continue;
                }

                // wait more messages
                if buffer.len() < buffer.capacity() {
                    sleep(self.cfg.wait).await;
                    if buffer.len() == 0 { continue; }
                }

                // persist messages
                let self_clone = self.clone();
                let batch = buffer.clone();
                buffer.clear();
                spawn(async move {
                    if let Err(e) = self_clone.persist(batch).await {
                        error!("failed to persist messages: {}", e);
                    }
                });
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
        let mut saved = HashMap::<String, (HashSet<String>, u16)>::new();
        for x in &vectors {
            if let Some(set) = saved.get_mut(&x.did) {
                set.0.insert(x.uid.clone());
                set.1 += 1;
            } else {
                saved.insert(x.did.clone(), (HashSet::from([x.uid.clone()]), 1));
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
        for (did, p) in saved {
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

            // flush content
            let content = self.db.collection::<Document>(COLL_CONTENT_NAME)
                .find_one_and_update(
                    doc! { "did": did.clone() },
                    doc! { "$inc": { "waitFlush": p.1 as i32 } },
                ).projection(doc! { "waitFlush": 1 }).await?;
            if let Some(v) = content {
                if v.get_i32("waitFlush").unwrap_or(0) >= self.cfg.flush_size as i32 {
                    let db = self.db.clone();
                    let did = did.clone();
                    spawn(async move {
                        if let Err(err) = flush_content(&db, did).await {
                            error!("failed to flush content: {}", err);
                        }
                    });
                }
            }

            for uid in p.0 {
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
}