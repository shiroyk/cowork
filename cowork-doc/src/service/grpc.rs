use log::error;
use crate::service::flush_content;
use doc_api::doc_service_server::{DocService, DocServiceServer};
use doc_api::DocContent as Content;
use mongodb::Database;
use tonic::transport::Server;
use tonic::{Request, Response, Status};

pub struct DocServiceImpl {
    pub db: Database,
}

#[tonic::async_trait]
impl DocService for DocServiceImpl {
    async fn find_content_by_did(
        &self,
        request: Request<String>,
    ) -> Result<Response<Content>, Status> {
        let data = match flush_content(&self.db, request.into_inner()).await {
            Ok(x) => x,
            Err(e) => {
                error!("failed to flush content: {}", e);
                return Err(Status::internal(e.to_string()))
            },
        };
        Ok(Response::new(Content { data }))
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