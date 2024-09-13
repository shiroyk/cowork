fn main() {
    tonic_build::configure()
        .compile(&["proto/doc.proto"], &["proto"])
        .unwrap();
}