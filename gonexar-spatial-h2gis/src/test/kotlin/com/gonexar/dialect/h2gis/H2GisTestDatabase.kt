package com.gonexar.dialect.h2gis

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.h2gis.functions.factory.H2GISFunctions
import org.hibernate.cfg.AvailableSettings
import org.hibernate.cfg.Configuration
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.WKTReader
import java.sql.DriverManager
import java.time.LocalDate

/**
 * In-memory H2 + H2GIS database bootstrapped with [GonexarH2GisDialect],
 * shared by all integration tests in this module.
 */
object H2GisTestDatabase {

    private const val URL = "jdbc:h2:mem:gonexar_h2gis;DB_CLOSE_DELAY=-1"

    private val wkt = WKTReader()

    fun geom(text: String, srid: Int = 4326): Geometry =
        wkt.read(text).also { it.srid = srid }

    val emf: EntityManagerFactory by lazy {
        DriverManager.getConnection(URL).use { H2GISFunctions.load(it) }

        val sessionFactory = Configuration()
            .addAnnotatedClass(Area::class.java)
            .addAnnotatedClass(Route::class.java)
            .setProperty(AvailableSettings.JAKARTA_JDBC_URL, URL)
            .setProperty(AvailableSettings.DIALECT, GonexarH2GisDialect::class.java.name)
            .setProperty(AvailableSettings.HBM2DDL_AUTO, "create-drop")
            .setProperty(AvailableSettings.SHOW_SQL, "false")
            .buildSessionFactory()

        sessionFactory.also { seed(it) }
    }

    /**
     * Fixtures (planar coordinates, SRID 4326):
     *
     *  - `square`   (0,0)-(10,10)
     *  - `inner`    (2,2)-(4,4), inside `square`
     *  - `neighbor` (10,0)-(20,10), shares the x = 10 edge with `square`
     *  - `far`      (100,100)-(110,110), disjoint from everything
     *  - `diagonal` route from (0,0) to (20,10)
     *  - `straight` route from (0,20) to (10,20), length 10
     */
    private fun seed(emf: EntityManagerFactory) = inTransaction(emf) { em ->
        em.persist(Area(1, "square", geom("POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))"), LocalDate.of(2024, 3, 15)))
        em.persist(Area(2, "inner", geom("POLYGON((2 2, 4 2, 4 4, 2 4, 2 2))"), LocalDate.of(2023, 11, 2)))
        em.persist(Area(3, "neighbor", geom("POLYGON((10 0, 20 0, 20 10, 10 10, 10 0))"), LocalDate.of(2025, 1, 31)))
        em.persist(Area(4, "far", geom("POLYGON((100 100, 110 100, 110 110, 100 110, 100 100))"), null))
        em.persist(Route(1, "diagonal", geom("LINESTRING(0 0, 20 10)")))
        em.persist(Route(2, "straight", geom("LINESTRING(0 20, 10 20)")))
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
