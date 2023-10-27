rootProject.name = "cowork"

pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include("cowork-common")
include("cowork-user:api")
include("cowork-user:service")
include("cowork-doc:api")
include("cowork-doc:service")
