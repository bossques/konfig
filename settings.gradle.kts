enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    plugins {
        kotlin("jvm") version "1.5.20"
        kotlin("plugin.serialization") version "1.5.20"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

rootProject.name = "Konfig"
include("core")
include("yaml")
