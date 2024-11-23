const RED: &str = "31";
const GREEN: &str = "32";
const YELLOW: &str = "33";
const BLUE: &str = "36";
const GREY: &str = "38";

fn color(i: &str, str: &str) -> String { format!("\x1b[{}m{}\x1b[0m", i, str) }

pub fn red(str: &str) -> String { color(RED, str) }

pub fn green(str: &str) -> String { color(GREEN, str) }

pub fn yellow(str: &str) -> String { color(YELLOW, str) }

pub fn blue(str: &str) -> String { color(BLUE, str) }

pub fn grey(str: &str) -> String { color(GREY, str) }