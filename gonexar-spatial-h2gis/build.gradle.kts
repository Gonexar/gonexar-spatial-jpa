plugins {
    kotlin("plugin.jpa")
}

description = "Gonexar Spatial JPA: dialeto Hibernate para H2 + H2GIS, voltado a testes sem Docker."

dependencies {
    // Depende do core — traz JTS, Hibernate, JPA transitivamente
    api(project(":gonexar-spatial-core"))

    // H2GIS — banco em memória com extensão espacial (uso em testes)
    implementation("org.orbisgis:h2gis:2.2.3")
}
