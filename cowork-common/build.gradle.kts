plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.jackson.core)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.data.commons)
    implementation(libs.grpc.server.starter)
}