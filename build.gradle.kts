plugins {
    `version-catalog`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency)
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

catalog {
    versionCatalog {
        from(files("libs.versions.toml"))
    }
}

extra["nativeImageGrpcArgs"] = listOf(
    "--initialize-at-build-time=ch.qos.logback",
    "--initialize-at-build-time=org.conscrypt",
    "--initialize-at-build-time=org.conscrypt",
    "--initialize-at-build-time=org.slf4j.LoggerFactory",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl.OpenSsl",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.SSL",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.CertificateVerifier",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.SSLPrivateKeyMethod",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.AsyncSSLPrivateKeyMethod",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.CertificateCompressionAlgo",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.grpc.netty",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.channel.epoll",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.channel.unix",
    "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl",
    "--initialize-at-run-time=io.grpc.internal.RetriableStream",
)

subprojects {
    apply {
        plugin(rootProject.libs.plugins.spring.dependency.get().pluginId)
    }
    dependencyManagement {
        imports {
            mavenBom(rootProject.libs.spring.boot.bom.get().toString())
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("version-catalog") {
            groupId = "$group"
            artifactId = project.name
            version = version
            from(components["versionCatalog"])
        }
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}