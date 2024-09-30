use actix_web::error::ParseError;
use actix_web::http::header::{Header, HeaderName, HeaderValue, InvalidHeaderValue, TryIntoHeaderValue};
use actix_web::http::StatusCode;
use actix_web::{HttpMessage, HttpResponse, HttpResponseBuilder, ResponseError};
use mongodb::error::{Error, WriteFailure};
use std::collections::HashMap;
use std::fmt::{Display, Formatter};

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

pub struct UserID(String);

impl UserID {
    pub fn into_inner(self) -> String { self.0 }
}

impl TryIntoHeaderValue for UserID {
    type Error = InvalidHeaderValue;

    fn try_into_value(self) -> Result<HeaderValue, Self::Error> {
        HeaderValue::try_from(self.0)
    }
}

impl Header for UserID {
    fn name() -> HeaderName {
        "X-User-ID".parse().unwrap()
    }

    fn parse<M: HttpMessage>(msg: &M) -> Result<Self, ParseError> {
        let header = msg.headers().get(Self::name());
        if let Some(header) = header {
            Ok(UserID(header.to_str().unwrap().to_string()))
        } else {
            Err(ParseError::Header)
        }
    }

}