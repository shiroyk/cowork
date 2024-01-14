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