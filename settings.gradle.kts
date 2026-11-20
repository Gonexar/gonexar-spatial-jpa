rootProject.name = "gonexar-spatial-jpa"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Baixa o JDK do toolchain (17) automaticamente quando não estiver instalado.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    "gonexar-spatial-core",
    "gonexar-spatial-postgis",
    "gonexar-spatial-h2gis"
)
