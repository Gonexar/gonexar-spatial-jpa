plugins {
    kotlin("plugin.jpa")
}

description = "Gonexar Spatial JPA: DSL Kotlin type-safe para consultas espaciais com JPA Criteria e Hibernate Spatial."

dependencies {
    // JTS Geometry — API pública para consumidores do core
    api("org.locationtech.jts:jts-core:1.20.0")

    // Hibernate Core + Spatial — base para mapeamento de tipos e Criteria API
    api("org.hibernate.orm:hibernate-core:6.6.18.Final")
    api("org.hibernate.orm:hibernate-spatial:6.6.18.Final")

    // JPA API
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")
}
