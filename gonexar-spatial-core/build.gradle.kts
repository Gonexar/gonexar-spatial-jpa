plugins {
    kotlin("plugin.jpa")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))

    // JTS Geometry — API pública para consumidores do core
    api("org.locationtech.jts:jts-core:1.20.0")

    // Hibernate Core + Spatial — base para mapeamento de tipos e Criteria API
    api("org.hibernate.orm:hibernate-core:6.6.18.Final")
    api("org.hibernate.orm:hibernate-spatial:6.6.18.Final")

    // JPA API
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")

    // Jackson — para extensões de serialização
    api("com.fasterxml.jackson.core:jackson-databind:2.21.1")

    // Lombok (opcional)
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "gonexar-spatial-core"
        }
    }
}
