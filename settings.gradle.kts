rootProject.name = "gonexar-spatial-jpa"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/gonexar/gonexar-spatial-jpa")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

include(
    "gonexar-spatial-core",
    "gonexar-spatial-postgis",
    "gonexar-spatial-h2gis"
)
