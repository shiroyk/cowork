use crate::handler::error::HttpError;
use crate::model::{Doc, DocQuery};
use crate::service::{create, delete, find_by_id, search, update};
use actix_web::{delete, get, post, put, web, HttpResponse};
use mongodb::Database;
use std::collections::HashMap;

#[get("/api")]
async fn doc_search(db: web::Data<Database>, query: web::Query<DocQuery>) -> Result<HttpResponse, HttpError> {
    let x = search(db.get_ref(), query.into_inner()).await?;
    Ok(HttpResponse::Ok().json(x))
}

#[post("/api")]
async fn doc_create(db: web::Data<Database>, doc: web::Json<Doc>) -> Result<HttpResponse, HttpError> {
    let x = create(db.get_ref(), doc.into_inner()).await?;
    Ok(HttpResponse::Ok().json(HashMap::from([("id", x)])))
}

#[get("/api/{id}")]
async fn doc_single(db: web::Data<Database>, id: web::Path<String>) -> Result<HttpResponse, HttpError> {
    let x = find_by_id(db.get_ref(), id.into_inner()).await?;
    Ok(HttpResponse::Ok().json(x))
}

#[put("/api")]
async fn doc_update(db: web::Data<Database>, doc: web::Json<Doc>) -> Result<HttpResponse, HttpError> {
    update(db.get_ref(), doc.into_inner()).await?;
    Ok(HttpResponse::NoContent().finish())
}

#[delete("/api/{id}")]
async fn doc_delete(db: web::Data<Database>, id: web::Path<String>) -> Result<HttpResponse, HttpError> {
    delete(db.get_ref(), id.into_inner()).await?;
    Ok(HttpResponse::NoContent().finish())
}

pub fn init_handler(cfg: &mut web::ServiceConfig) {
    cfg.service(web::resource("/ping").route(web::to(|| { HttpResponse::NoContent() })))
        .service(doc_search)
        .service(doc_create)
        .service(doc_single)
        .service(doc_update)
        .service(doc_delete);
}