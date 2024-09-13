use actix_web::{web, App, HttpServer};
use doc::config::{connect_db, load_config};
use doc::handler::init_handler;
use doc::model::DB_NAME;
use doc::service::grpc_server;
use doc::stream::EventStream;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let config = load_config();

    let client = connect_db(&config.mongodb).await;
    let database = client.database(DB_NAME);
    EventStream::new(config.clone(), database.clone()).await.start();
    grpc_server(config.grpc, database.clone());

    println!("=> doc http started on {}", common::green(&config.http));
    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(database.clone()))
            .configure(init_handler)
    })
        .bind(config.http)?
        .run()
        .await
}