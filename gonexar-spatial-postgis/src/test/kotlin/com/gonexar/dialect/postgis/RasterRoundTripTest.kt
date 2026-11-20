package com.gonexar.dialect.postgis

import com.gonexar.dialect.postgis.PostgisTestDatabase.geom
import com.gonexar.dialect.postgis.PostgisTestDatabase.inTransaction
import com.gonexar.spatial.SpatialContext
import com.gonexar.spatial.query
import com.gonexar.type.Raster
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Raster values must travel between PostGIS and [Raster] as WKB bytes (issue #14).
 */
class RasterRoundTripTest {

    /** WKB of the seeded tile, as PostGIS serialises it. */
    private fun seededWkb(): ByteArray = inTransaction { em ->
        em.createNativeQuery("SELECT ST_AsBinary(rast) FROM elevation_tile WHERE id = 1").singleResult as ByteArray
    }

    @Test
    fun `reading a raster column returns WKB bytes`() {
        val tile = inTransaction { em -> em.find(ElevationTile::class.java, 1L) }
        assertThat(tile.rast!!.bytes).isEqualTo(seededWkb())
    }

    @Test
    fun `writing a raster stores it as a PostGIS raster`() {
        inTransaction { em -> em.persist(ElevationTile(100, Raster(seededWkb()))) }

        val (width, sum) = inTransaction { em ->
            val row = em.createNativeQuery(
                "SELECT ST_Width(rast), (ST_SummaryStats(rast)).sum FROM elevation_tile WHERE id = 100"
            ).singleResult as Array<*>
            (row[0] as Number).toInt() to (row[1] as Number).toDouble()
        }
        assertThat(width).isEqualTo(3)
        assertThat(sum).isEqualTo(45.0)

        val reloaded = inTransaction { em -> em.find(ElevationTile::class.java, 100L) }
        assertThat(reloaded.rast!!.bytes).isEqualTo(seededWkb())

        inTransaction { em -> em.remove(em.find(ElevationTile::class.java, 100L)) }
    }

    @Test
    fun `raster projections from ST_Clip return WKB bytes`() {
        val clipped = inTransaction { em ->
            query<ElevationTile, Raster>(SpatialContext(em, ElevationTile::class.java), Raster::class.java) {
                val tile = raster("rast")
                resultProjection(tile.stClip(this, toGeometryExpr(geom("POLYGON((0 0, 3 0, 3 3, 0 3, 0 0))"))).expr)
            }.single()
        }
        val expected = inTransaction { em ->
            em.createNativeQuery(
                "SELECT ST_AsBinary(ST_Clip(rast, ST_GeomFromText('POLYGON((0 0, 3 0, 3 3, 0 3, 0 0))', 4326))) FROM elevation_tile WHERE id = 1"
            ).singleResult as ByteArray
        }
        assertThat(clipped.bytes).isEqualTo(expected)
    }
}
