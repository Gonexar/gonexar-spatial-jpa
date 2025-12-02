plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
}

group = "com.gonexar"
version = "0.1.0-alpha"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // Kotlin
    implementation(kotlin("stdlib"))
    // JTS Geometry
    api("org.locationtech.jts:jts-core:1.19.0")
    // PostGIS JDBC
    api("net.postgis:postgis-jdbc:2.5.0")
    api("org.postgresql:postgresql:42.7.7")
    // Hibernate Core
    api("org.hibernate.orm:hibernate-core:6.4.1.Final")
    api("org.hibernate.orm:hibernate-spatial:6.4.1.Final")
    // Jackson
    api("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    // JPA API
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")
    // Lombok (opcional)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.gonexar"
            artifactId = "gonexar-spatial-jpa"
            version = "0.1.0-alpha"
        }
    }

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