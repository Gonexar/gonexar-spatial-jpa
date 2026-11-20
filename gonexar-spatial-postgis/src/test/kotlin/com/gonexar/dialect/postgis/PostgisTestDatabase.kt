package com.gonexar.dialect.postgis

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.AvailableSettings
import org.hibernate.cfg.Configuration
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.WKTReader
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

/**
 * PostgreSQL + PostGIS (with raster) running in a Testcontainers container, shared by all
 * integration tests in this module and bootstrapped with [GonexarPostgisDialect].
 *
 * The image can be overridden with the `gonexar.postgis.image` system property.
 */
object PostgisTestDatabase {

    private val image = DockerImageName
        .parse(System.getProperty("gonexar.postgis.image", "postgis/postgis:16-3.5"))
        .asCompatibleSubstituteFor("postgres")

    val container: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer(image).also { it.start() }
    }

    private val wkt = WKTReader()

    fun geom(text: String, srid: Int = 4326): Geometry =
        wkt.read(text).also { it.srid = srid }

    val emf: EntityManagerFactory by lazy {
        DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { c ->
            c.createStatement().use { it.execute("CREATE EXTENSION IF NOT EXISTS postgis_raster") }
        }

        Configuration()
            .addAnnotatedClass(Area::class.java)
            .addAnnotatedClass(Route::class.java)
            .addAnnotatedClass(ElevationTile::class.java)
            .setProperty(AvailableSettings.JAKARTA_JDBC_URL, container.jdbcUrl)
            .setProperty(AvailableSettings.JAKARTA_JDBC_USER, container.username)
            .setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, container.password)
            .setProperty(AvailableSettings.DIALECT, GonexarPostgisDialect::class.java.name)
            .setProperty(AvailableSettings.HBM2DDL_AUTO, "create-drop")
            .buildSessionFactory()
            .also { seed(it) }
    }

    /**
     * Fixtures in WGS 84 (SRID 4326), near the equator so that one degree is about 111 km:
     *
     *  - `cell`     (0,0)-(0.01,0.01), about 1.11 km x 1.11 km
     *  - `east`     (0.02,0)-(0.03,0.01), about 1.1 km east of `cell`
     *  - `far`      (10,10)-(10.01,10.01)
     *  - `meridian` route from (0,0) to (0,0.01), about 1.1 km long
     *  - `elevation_tile` 1: a 3x3 raster over (0,0)-(3,3), cell size 1, values 1..9
     *    (row-major from the top-left), nodata = -9999
     */
    private fun seed(emf: EntityManagerFactory) = inTransaction(emf) { em ->
        em.persist(Area(1, "cell", geom("POLYGON((0 0, 0.01 0, 0.01 0.01, 0 0.01, 0 0))")))
        em.persist(Area(2, "east", geom("POLYGON((0.02 0, 0.03 0, 0.03 0.01, 0.02 0.01, 0.02 0))")))
        em.persist(Area(3, "far", geom("POLYGON((10 10, 10.01 10, 10.01 10.01, 10 10.01, 10 10))")))
        em.persist(Route(1, "meridian", geom("LINESTRING(0 0, 0 0.01)")))
        em.flush()

        em.createNativeQuery(
            """
            INSERT INTO elevation_tile (id, rast)
            SELECT 1, ST_SetValues(
                ST_AddBand(ST_MakeEmptyRaster(3, 3, 0, 3, 1, -1, 0, 0, 4326), '32BF'::text, 0, -9999),
                1, 1, 1,
                ARRAY[[1, 2, 3], [4, 5, 6], [7, 8, 9]]::double precision[][]
            )
            """.trimIndent()
        ).executeUpdate()
    }

    fun <T> inTransaction(emf: EntityManagerFactory = this.emf, block: (EntityManager) -> T): T {
        val em = emf.createEntityManager()
        try {
            em.transaction.begin()
            val result = block(em)
            em.transaction.commit()
            return result
        } finally {
            if (em.transaction.isActive) em.transaction.rollback()
            em.close()
        }
    }
}
