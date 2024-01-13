plugins {
    alias(libs.plugins.spring)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graalvm.native)
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.grpc.server.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.google.native.image.support)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(project(mapOf("path" to ":cowork-user:api")))
    implementation(project(mapOf("path" to ":cowork-common")))
}

springBoot {
    mainClass = "user.service.UserApplicationKt"
}

graalvmNative {
    toolchainDetection = true
    binaries {
        named("main") {
            buildArgs.add("--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl.BouncyCastleAlpnSslUtils")
            buildArgs.add("--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.CertificateCompressionAlgo")
            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("-J-Xmx4G")
            imageName = "app"
            quickBuild = true
        }
    }
}