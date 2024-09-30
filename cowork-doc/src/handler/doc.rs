use crate::handler::util::{HttpError, UserID};
use crate::model::{Doc, DocQuery};
use crate::service::{create, delete, doc_vector, find_by_id, search, update};
use actix_web::{delete, get, post, put, web, HttpResponse};
use mongodb::Database;

#[get("/api")]
async fn doc_search(db: web::Data<Database>, query: web::Query<DocQuery>) -> Result<HttpResponse, HttpError> {
    let x = search(db.get_ref(), query.into_inner()).await?;
    Ok(HttpResponse::Ok().json(x))
}

 #[post("/api")]
async fn doc_create(db: web::Data<Database>, doc: web::Json<Doc>, id: web::Header<UserID>) -> Result<HttpResponse, HttpError> {
    let mut doc = doc.into_inner();
    doc.uid = id.0.into_inner();
    let x = create(db.get_ref(), doc).await?;
    Ok(HttpResponse::Ok().json(x))
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

#[get("/api/{id}/vectors")]
async fn doc_vector_get(db: web::Data<Database>, id: web::Path<String>) -> Result<HttpResponse, HttpError> {
    let data = doc_vector(db.get_ref(), id.into_inner()).await?;
    Ok(HttpResponse::Ok().json(data))
}

pub fn init_handler(cfg: &mut web::ServiceConfig) {
    cfg.service(web::resource("/ping").route(web::to(|| { HttpResponse::NoContent() })))
        .service(doc_search)
        .service(doc_create)
        .service(doc_single)
        .service(doc_update)
        .service(doc_delete)
        .service(doc_vector_get);
}