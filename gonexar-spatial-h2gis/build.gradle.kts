plugins {
    kotlin("plugin.jpa")
}

dependencies {
    // Depende do core — traz JTS, Hibernate, JPA transitivamente
    api(project(":gonexar-spatial-core"))

    // H2GIS — banco em memória com extensão espacial (uso em testes)
    implementation("org.orbisgis:h2gis:2.2.3")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "gonexar-spatial-h2gis"
        }
    }
}
