plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.javax.annotation)
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
    api(libs.grpc.client.starter)
}

sourceSets {
    main {
        java {
            srcDirs("src/generated/java")
        }
    }
}