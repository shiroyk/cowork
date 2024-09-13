use std::collections::HashMap;
use std::fmt::{Display, Formatter};
use actix_web::http::StatusCode;
use actix_web::{HttpResponse, HttpResponseBuilder, ResponseError};
use mongodb::error::{Error, WriteFailure};

#[derive(Debug)]
pub struct HttpError {
    pub code: StatusCode,
    pub msg: String,
}

impl Display for HttpError {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}: {}", self.code, self.msg)
    }
}

impl From<Error> for HttpError {
    fn from(err: Error) -> HttpError {
        let mut code = StatusCode::INTERNAL_SERVER_ERROR;
        let msg = match &*err.kind {
            mongodb::error::ErrorKind::Write(e) => {
                match e {
                    WriteFailure::WriteConcernError(e) => e.message.clone(),
                    WriteFailure::WriteError(e) => {
                        if e.code == 11000 {
                            code = StatusCode::BAD_REQUEST;
                        }
                        e.message.clone()
                    }
                    _ => err.to_string(),
                }
            }
            _ => err.to_string(),
        };
        HttpError { code, msg }
    }
}

impl ResponseError for HttpError {
    fn error_response(&self) -> HttpResponse {
        HttpResponseBuilder::new(self.code).json(HashMap::from([("error", &self.msg)]))
    }
}