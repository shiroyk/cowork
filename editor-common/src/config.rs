use std::env;
use std::sync::{Once};
use regex::Regex;

const CONFIG_FILE: &str = "/etc/app/config.yaml";

fn expand_env(input: &str) -> String {
    let re = Regex::new(r"\$([A-Za-z0-9_]+)|\$\{([A-Za-z0-9_]+)}").unwrap();

    re.replace_all(input, |caps: &regex::Captures| {
        let var_name = caps.get(1).or(caps.get(2)).map_or("", |m| m.as_str());
        env::var(var_name).unwrap_or_default()
    }).to_string()
}

pub fn read_config() -> Result<String, std::io::Error> {
    let cfg = std::fs::read_to_string(CONFIG_FILE)?;
    Ok(expand_env(&cfg))
}

static mut PROFILE_ENV: Option<String> = None;
static INIT: Once = Once::new();

fn get_profile_env() -> &'static str {
    INIT.call_once(|| {
        let value = env::var("PROFILE").unwrap_or_else(|_| "dev".to_string());

        unsafe {
            PROFILE_ENV = Some(value);
        }
    });

    unsafe {
        PROFILE_ENV.as_deref().unwrap()
    }
}

pub fn is_dev() -> bool { !is_k8s() }

pub fn is_k8s() -> bool { get_profile_env().eq_ignore_ascii_case("k8s") }

pub fn profile_value<T>(dev: T, k8s: T) -> T {
    if is_dev() {
        dev
    } else {
        k8s
    }
}