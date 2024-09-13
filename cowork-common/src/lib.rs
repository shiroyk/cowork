mod color;
mod config;

pub use crate::{
    color::{red, green, yellow, blue, grey},
    config::{read_config, is_k8s, is_dev, profile_value},
};