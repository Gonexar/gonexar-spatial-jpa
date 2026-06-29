plugins {
    kotlin("jvm") version "2.2.0" apply false
    kotlin("plugin.jpa") version "2.2.0" apply false
    `maven-publish`
}

group = "com.gonexar"
version = "0.2.0-alpha"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = "com.gonexar"
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("https://maven.pkg.github.com/gonexar/gonexar-spatial-jpa")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
