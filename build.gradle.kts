plugins {
    `java-library`
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
}

group = "com.gonexar"
version = "1.0.0"

java {
    withSourcesJar()
    withJavadocJar()
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
    api("net.postgis:postgis-jdbc:2.6.0")
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
