use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::fmt::Debug;
use bytes::Bytes;
use serde_repr::{Deserialize_repr, Serialize_repr};

pub const DB_NAME: &str = "docs";
pub const COLL_DOC_NAME: &str = "doc";
pub const COLL_VECTOR_NAME: &str = "vector";
pub const COLL_CONTENT_NAME: &str = "content";

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Doc {
    #[serde(default = "default_did")]
    pub did: String,
    pub title: String,
    pub uid: String,
    #[serde(default)]
    pub clients: Option<HashMap<String, u64>>,
    #[serde(default)]
    pub trash: bool,
    #[serde(default)]
    pub last_updated: i64,
    #[serde(default)]
    pub created_at: i64,
    #[serde(default)]
    pub updated_at: i64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DocQuery {
    pub limit: Option<i64>,
    pub offset: Option<u64>,
    pub title: Option<String>,
    pub trash: Option<bool>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DocVector {
    pub did: String,
    pub uid: String,
    pub data: Bytes,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DocContent {
    pub did: String,
    pub data: Bytes,
}

fn default_did() -> String { mongodb::bson::oid::ObjectId::new().to_string() }

#[derive(Debug, Clone, Serialize_repr, Deserialize_repr)]
#[repr(u8)]
pub enum Event {
    Login = 1,
    Logout = 2,
    Sync = 3,
    Update = 4,
    Save = 5,
}

impl Default for Event {
    fn default() -> Self { Event::Update }
}

impl Event {
    pub fn subject(&self) -> String {
        match self {
            Event::Login => "events.login",
            Event::Logout => "events.logout",
            Event::Sync => "events.sync",
            Event::Update => "events.update",
            Event::Save => "events.save",
        }.to_string()
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CollabMessage {
    #[serde(default)]
    pub event: Event,
    pub uid: String,
    pub did: String,
    pub data: Bytes,
}