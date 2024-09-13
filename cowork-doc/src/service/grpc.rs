use crate::model::{Doc, DocVector, COLL_DOC_NAME, COLL_VECTOR_NAME};
use doc_api::doc_service_server::{DocService, DocServiceServer};
use doc_api::{DocNodes, VerifyRequest, VerifyResponse};
use mongodb::bson::doc;
use mongodb::Database;
use tonic::codegen::tokio_stream::StreamExt;
use tonic::transport::Server;
use tonic::{Request, Response, Status};

pub struct DocServiceImpl {
    pub db: Database,
}

#[tonic::async_trait]
impl DocService for DocServiceImpl {
    async fn verify_permission(
        &self,
        request: Request<VerifyRequest>,
    ) -> Result<Response<VerifyResponse>, Status> {
        let req = request.into_inner();
        let doc = self.db.collection::<Doc>(COLL_DOC_NAME)
            .find_one(doc! { "did": req.did }).await.unwrap();
        if let Some(doc) = doc {
            return Ok(Response::new(VerifyResponse { ok: doc.uid == req.uid, msg: "".to_string() }));
        }
        Ok(Response::new(VerifyResponse { ok: false, msg: "doc not exists".to_string() }))
    }

    async fn find_nodes_by_did(
        &self,
        request: Request<String>,
    ) -> Result<Response<DocNodes>, Status> {
        let result = self.db.collection::<DocVector>(COLL_VECTOR_NAME)
            .find(doc! { "did": request.into_inner() })
            .await.unwrap();
        let data = result.map(|x| x.unwrap().data.to_vec())
            .collect::<Vec<Vec<u8>>>().await;
        Ok(Response::new(DocNodes { nodes: data }))
    }
}

pub fn grpc_server(uri: String, db: Database) {
    tokio::spawn(async move {
        let addr = uri.parse().expect("failed to parse grpc address");
        println!("=> doc grpc started on {}", common::green(&uri));
        let _ = Server::builder()
            .add_service(DocServiceServer::new(DocServiceImpl { db }))
            .serve(addr)
            .await;
    });
}