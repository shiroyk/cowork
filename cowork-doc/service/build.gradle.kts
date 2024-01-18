plugins {
    alias(libs.plugins.spring)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graalvm.native)
}

dependencies {
    implementation(libs.jnats)
    implementation(libs.msgpack)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutines)
    implementation(libs.grpc.server.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.nats.spring.boot.starter)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(project(mapOf("path" to ":cowork-common")))
    implementation(project(mapOf("path" to ":cowork-doc:api")))
}

springBoot {
    mainClass = "doc.service.DocApplicationKt"
}

graalvmNative {
    toolchainDetection = true
    binaries {
        named("main") {
            val nativeImageGrpcArgs: List<String> by rootProject.extra
            buildArgs.addAll(nativeImageGrpcArgs)
            buildArgs.add("-J-Xmx4G")
            imageName = "app"
            quickBuild = true
        }
    }
}