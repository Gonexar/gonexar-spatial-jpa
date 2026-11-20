plugins {
    kotlin("plugin.jpa")
}

description = "Gonexar Spatial JPA: dialeto Hibernate para PostgreSQL + PostGIS (geometria, geography e raster)."

dependencies {
    // Depende do core — traz JTS, Hibernate, JPA transitivamente
    api(project(":gonexar-spatial-core"))

    // PostGIS JDBC — driver EWKB específico do PostgreSQL + PostGIS
    api("net.postgis:postgis-jdbc:2024.1.0")

    // Driver JDBC do PostgreSQL, necessário só em runtime. A aplicação pode
    // sobrescrever a versão declarando o driver diretamente.
    runtimeOnly("org.postgresql:postgresql:42.7.7")
}

dependencies {
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.4"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
}

// Repassa -Dgonexar.postgis.image=<imagem> da linha de comando do Gradle para a JVM de teste;
// sem ele, PostgisTestDatabase usa a imagem padrão.
tasks.withType<Test>().configureEach {
    providers.systemProperty("gonexar.postgis.image").orNull?.let { systemProperty("gonexar.postgis.image", it) }
}
