use crate::service::create_index;
use common::{is_dev, read_config};
use mongodb::options::ClientOptions;
use mongodb::Client;
use serde::{Deserialize, Serialize};
use std::time::Duration;

const BANNER: &str = r#"
  ____
 |  _ \  ___   ___
 | | | |/ _ \ / __|
 | |_| | (_) | (__
 |____/ \___/ \___|
"#;

#[derive(Debug, Clone, Serialize, Deserialize, Copy)]
pub struct StreamConfig {
    #[serde(default = "default_batch")]
    pub batch: usize,
    #[serde(default = "default_wait", deserialize_with = "deserialize_duration")]
    pub wait: Duration,
    #[serde(default = "default_max_age", deserialize_with = "deserialize_duration")]
    pub max_age: Duration,
}

impl Default for StreamConfig {
    fn default() -> Self {
        StreamConfig {
            batch: 16,
            wait: Duration::from_secs(5),
            max_age: Duration::from_secs(60),
        }
    }
}

fn default_batch() -> usize { 16 }

fn default_wait() -> Duration { Duration::from_secs(5) }

fn default_max_age() -> Duration { Duration::from_secs(60) }

fn deserialize_duration<'de, D>(deserializer: D) -> Result<Duration, D::Error>
where
    D: serde::Deserializer<'de>,
{
    let secs: u64 = Deserialize::deserialize(deserializer)?;
    Ok(Duration::from_secs(secs))
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Config {
    #[serde(default = "default_http")]
    pub http: String,
    #[serde(default = "default_grpc")]
    pub grpc: String,
    pub nats: String,
    pub mongodb: String,
    #[serde(default)]
    pub stream: StreamConfig,
}

fn default_http() -> String { "0.0.0.0:8080".to_string() }

fn default_grpc() -> String { "0.0.0.0:9090".to_string() }

pub fn load_config() -> Config {
    println!("{}", common::blue(BANNER));
    if is_dev() {
        return Config {
            http: "127.0.0.1:8083".to_string(),
            grpc: "127.0.0.1:9093".to_string(),
            nats: "nats://localhost:4222".to_string(),
            mongodb: "mongodb://dev:123456@localhost:27017/dev?authSource=admin&directConnection=true".to_string(),
            stream: StreamConfig::default(),
        }
    }
    let result = read_config().expect("failed to read config file");
    serde_yaml::from_str::<Config>(result.as_str()).expect("failed to parse config file")
}

pub async fn connect_db(uri: &str) -> Client {
    let options = ClientOptions::parse(uri).await.expect("failed to parse db uri");
    let client = Client::with_options(options).expect("failed to connect to database");
    create_index(&client).await;
    println!("=> doc db connected successfully");
    client
}