plugins {
    kotlin("plugin.jpa")
}

dependencies {
    // Depende do core — traz JTS, Hibernate, JPA transitivamente
    api(project(":gonexar-spatial-core"))

    // PostGIS JDBC — driver EWKB específico do PostgreSQL + PostGIS
    api("net.postgis:postgis-jdbc:2024.1.0")
    api("org.postgresql:postgresql:42.7.7")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "gonexar-spatial-postgis"
        }
    }
}
