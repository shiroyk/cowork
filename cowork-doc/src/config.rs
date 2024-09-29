use crate::service::create_index;
use common::{is_dev, read_config};
use log::{Level, Metadata, Record};
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
    pub batch: usize,       // length of persistent events in batch
    #[serde(default = "default_wait", deserialize_with = "deserialize_duration")]
    pub wait: Duration,     // time to wait batch full
    #[serde(default = "default_max_age", deserialize_with = "deserialize_duration")]
    pub max_age: Duration,  // max age of event message
    #[serde(default = "default_flush_size")]
    pub flush_size: u16,  // size of flush doc content
}

impl Default for StreamConfig {
    fn default() -> Self {
        StreamConfig {
            batch: default_batch(),
            wait: default_wait(),
            max_age: default_max_age(),
            flush_size: default_flush_size(),
        }
    }
}

fn default_batch() -> usize { 64 }

fn default_wait() -> Duration { Duration::from_secs(5) }

fn default_max_age() -> Duration { Duration::from_secs(60) }

fn default_flush_size() -> u16 { 128 }

fn deserialize_duration<'de, D>(deserializer: D) -> Result<Duration, D::Error>
where
    D: serde::Deserializer<'de>,
{
    let secs: u64 = Deserialize::deserialize(deserializer)?;
    Ok(Duration::from_secs(secs))
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Config {
    #[serde(default = "default_level", rename = "log-level")]
    pub log_level: Level,
    #[serde(default = "default_http")]
    pub http: String,
    #[serde(default = "default_grpc")]
    pub grpc: String,
    pub nats: String,
    pub mongodb: String,
    #[serde(default)]
    pub stream: StreamConfig,
}

fn default_level() -> Level { Level::Info }

fn default_http() -> String { "0.0.0.0:8080".to_string() }

fn default_grpc() -> String { "0.0.0.0:9090".to_string() }

struct SimpleLogger;

impl log::Log for SimpleLogger {
    fn enabled(&self, metadata: &Metadata) -> bool {
        metadata.level() <= Level::Info
    }

    fn log(&self, record: &Record) {
        if self.enabled(record.metadata()) {
            println!("{} - {}", record.level(), record.args());
        }
    }

    fn flush(&self) {}
}

static LOGGER: SimpleLogger = SimpleLogger;

pub fn load_config() -> Config {
    println!("{}", common::blue(BANNER));
    let config = if is_dev() {
        return Config {
            log_level: Level::Debug,
            http: "127.0.0.1:8083".to_string(),
            grpc: "127.0.0.1:9093".to_string(),
            nats: "nats://localhost:4222".to_string(),
            mongodb: "mongodb://dev:123456@localhost:27017/dev?authSource=admin&directConnection=true".to_string(),
            stream: StreamConfig::default(),
        };
    } else {
        let result = read_config().expect("failed to read config file");
        serde_yaml::from_str::<Config>(result.as_str()).expect("failed to parse config file")
    };
    log::set_logger(&LOGGER).map(|()| log::set_max_level(config.log_level.to_level_filter())).expect("failed to set logger");
    config
}

pub async fn connect_db(uri: &str) -> Client {
    let options = ClientOptions::parse(uri).await.expect("failed to parse db uri");
    let client = Client::with_options(options).expect("failed to connect to database");
    create_index(&client).await;
    println!("=> doc db connected successfully");
    client
}