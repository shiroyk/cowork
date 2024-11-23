use log::error;
use crate::service::{find_by_id, flush_content, get_doc_client_id};
use doc_api::doc_service_server::{DocService, DocServiceServer};
use doc_api::{Doc as DocDto, DocContent as Content, DocContentReq};
use mongodb::Database;
use tonic::transport::Server;
use tonic::{Request, Response, Status};

pub struct DocServiceImpl {
    pub db: Database,
}

#[tonic::async_trait]
impl DocService for DocServiceImpl {
    async fn find_content(
        &self,
        request: Request<DocContentReq>,
    ) -> Result<Response<Content>, Status> {
        let req = request.into_inner();
        let result = match get_doc_client_id(&self.db, req.did.clone(), req.uid).await {
            Ok(x) => x,
            Err(e) => {
                error!("failed to flush content: {}", e);
                return Err(Status::internal(e.to_string()))
            },
        };

        let client_id = match result {
            Some(x) => x,
            None => return Err(Status::not_found("doc not found")),
        };

        let data = match flush_content(&self.db, req.did).await {
            Ok(x) => x,
            Err(e) => {
                error!("failed to flush content: {}", e);
                return Err(Status::internal(e.to_string()))
            },
        };
        Ok(Response::new(Content { data, client_id }))
    }

    async fn find_doc(&self, request: Request<String>) -> Result<Response<DocDto>, Status> {
        let result = match find_by_id(&self.db, request.into_inner()).await {
            Ok(v) => v,
            Err(e) => {
                error!("failed to find doc: {}", e);
                return Err(Status::internal(e.to_string()))
            }
        };

        let doc = match result {
            Some(x) => x,
            None => return Err(Status::not_found("doc not found")),
        };

        Ok(Response::new(DocDto{
            did: doc.did,
            title: doc.title,
            uid: doc.uid,
            clients: doc.clients,
            trash: doc.trash,
            last_updated: doc.last_updated,
            created_at: doc.created_at,
            updated_at: doc.updated_at,
        }))
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