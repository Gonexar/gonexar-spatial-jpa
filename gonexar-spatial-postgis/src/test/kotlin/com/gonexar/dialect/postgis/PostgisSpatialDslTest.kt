package com.gonexar.dialect.postgis

import com.gonexar.dialect.postgis.PostgisTestDatabase.geom
import com.gonexar.dialect.postgis.PostgisTestDatabase.inTransaction
import com.gonexar.expression.SpatialExpr
import com.gonexar.spatial.SpatialContext
import com.gonexar.spatial.SpatialDslContainer
import com.gonexar.spatial.query
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.assertj.core.api.Assertions.withinPercentage
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * End-to-end tests of the spatial DSL against a real PostgreSQL + PostGIS server,
 * covering the PostGIS-only parts: geography metrics, line referencing and raster.
 */
@Testcontainers(disabledWithoutDocker = false)
class PostgisSpatialDslTest {

    private fun <R : Any> areas(resultClass: Class<R>, block: SpatialDslContainer<Area, R>.() -> Unit): List<R> =
        inTransaction { em -> query(SpatialContext(em, Area::class.java), resultClass, block) }

    private fun <R : Any> routes(resultClass: Class<R>, block: SpatialDslContainer<Route, R>.() -> Unit): List<R> =
        inTransaction { em -> query(SpatialContext(em, Route::class.java), resultClass, block) }

    private fun <R : Any> tiles(resultClass: Class<R>, block: SpatialDslContainer<ElevationTile, R>.() -> Unit): List<R> =
        inTransaction { em -> query(SpatialContext(em, ElevationTile::class.java), resultClass, block) }

    private fun SpatialDslContainer<*, *>.name() = entity.get<String>("name")

    private fun areaNumber(areaName: String, value: SpatialDslContainer<Area, NameAndNumber>.() -> SpatialExpr<Double>): Double =
        areas(NameAndNumber::class.java) {
            whereEq(name(), areaName)
            select<NameAndNumber> {
                expr(name())
                field(value())
            }
        }.single().value

    private fun areaNamesWhere(predicate: SpatialDslContainer<Area, Area>.() -> SpatialExpr<Boolean>): List<String> =
        areas(Area::class.java) { where(predicate().toPredicate()) }.map { it.name }.sorted()

    // ---------------------------------------------------------------------
    // Geography metrics (metres)
    // ---------------------------------------------------------------------

    @Test
    fun `buffer uses metres through a geography cast`() {
        // The point sits about 556 m from both `cell` and `east`.
        val point = geom("POINT(0.015 0.005)")

        assertThat(areaNamesWhere { toGeometryExpr(point).buffer(600.0).intersects(geomColumn("polygon")) })
            .containsExactly("cell", "east")
        assertThat(areaNamesWhere { toGeometryExpr(point).buffer(500.0).intersects(geomColumn("polygon")) })
            .isEmpty()
    }

    @Test
    fun `distance is geodesic in metres`() {
        val east = geom("POLYGON((0.02 0, 0.03 0, 0.03 0.01, 0.02 0.01, 0.02 0))")
        assertThat(areaNumber("cell") { geomColumn("polygon").distance(toGeometryExpr(east)) })
            .isCloseTo(1113.2, withinPercentage(0.5))
    }

    @Test
    fun `area and perimeter are geodesic`() {
        assertThat(areaNumber("cell") { geomColumn("polygon").area() })
            .isCloseTo(1_230_900.0, withinPercentage(1))
        assertThat(areaNumber("cell") { geomColumn("polygon").perimeter(useGeography = true) })
            .isCloseTo(4_437.0, withinPercentage(1))
        assertThat(areaNumber("cell") { geomColumn("polygon").perimeter() })
            .isCloseTo(0.04, offset(1e-12))
    }

    @Test
    fun `lengthGeography measures routes in metres`() {
        val length = routes(NameAndNumber::class.java) {
            whereEq(name(), "meridian")
            select<NameAndNumber> {
                expr(name())
                field(geomColumn("path").lengthGeography())
            }
        }.single().value
        assertThat(length).isCloseTo(1105.7, withinPercentage(0.5))
    }

    @Test
    fun `dWithin filters by distance in the geometry units`() {
        val point = geom("POINT(0.015 0.005)")
        assertThat(areaNamesWhere { geomColumn("polygon").dWithin(toGeometryExpr(point), 0.006) })
            .containsExactly("cell", "east")
        assertThat(areaNamesWhere { geomColumn("polygon").dWithin(toGeometryExpr(point), 0.004) })
            .isEmpty()
    }

    // ---------------------------------------------------------------------
    // Route analysis
    // ---------------------------------------------------------------------

    @Test
    fun `line referencing locates points along a route`() {
        val rows = routes(NameAndNumber::class.java) {
            whereEq(name(), "meridian")
            select<NameAndNumber> {
                expr(name())
                field(geomColumn("path").lineLocatePoint(toGeometryExpr(geom("POINT(0.001 0.0025)"))))
            }
        }
        assertThat(rows.single().value).isCloseTo(0.25, offset(1e-9))

        val midpoint = routes(NameAndText::class.java) {
            whereEq(name(), "meridian")
            select<NameAndText> {
                expr(name())
                field(geomColumn("path").interpolatePoint(literal(0.5)).asText())
            }
        }
        assertThat(midpoint.single().text).isEqualTo("POINT(0 0.005)")
    }

    @Test
    fun `bearingAlongRoute returns the route azimuth at the closest point`() {
        val rows = routes(NameAndNumber::class.java) {
            whereEq(name(), "meridian")
            val route = geomColumn("path")
            val closest = route.closestPoint(toGeometryExpr(geom("POINT(0.002 0.004)")))
            select<NameAndNumber> {
                expr(name())
                field(bearingAlongRoute(route, closest))
            }
        }
        // Heading north: azimuth 0 rad.
        assertThat(rows.single().value).isCloseTo(0.0, offset(1e-9))
    }

    @Test
    fun `percentageInsideIntersection relates two lengths`() {
        val rows = routes(NameAndNumber::class.java) {
            whereEq(name(), "meridian")
            val route = geomColumn("path")
            val half = toGeometryExpr(geom("POLYGON((-1 -1, 1 -1, 1 0.005, -1 0.005, -1 -1))"))
            select<NameAndNumber> {
                expr(name())
                field(percentageInsideIntersection(route.intersection(half).length(), route.length()))
            }
        }
        assertThat(rows.single().value).isCloseTo(50.0, offset(1e-6))
    }

    @Test
    fun `transform reprojects geometries`() {
        val rows = routes(NameAndGeometry::class.java) {
            whereEq(name(), "meridian")
            select<NameAndGeometry> {
                expr(name())
                field(geomColumn("path").endPoint().transform(3857))
            }
        }
        val point = rows.single().geometry
        assertThat(point.coordinate.x).isCloseTo(0.0, offset(1e-6))
        assertThat(point.coordinate.y).isCloseTo(1113.2, withinPercentage(0.5))
    }

    // ---------------------------------------------------------------------
    // Raster
    // ---------------------------------------------------------------------

    @Test
    fun `raster value is sampled at a point`() {
        val rows = tiles(TileValue::class.java) {
            select<TileValue> {
                expr(entity.get<Long>("id"))
                field(raster("rast").value(toGeometryExpr(geom("POINT(1.5 1.5)"))))
            }
        }
        assertThat(rows).containsExactly(TileValue(1, 5.0))
    }

    @Test
    fun `raster summary statistics are projected field by field`() {
        val rows = tiles(TileStats::class.java) {
            val stats = raster("rast").stSummaryStats()
            select<TileStats> {
                expr(entity.get<Long>("id"))
                field(stats.count)
                field(stats.sum)
                field(stats.mean)
                field(stats.stddev)
                field(stats.min)
                field(stats.max)
            }
        }
        val s = rows.single()
        assertThat(s.count).isEqualTo(9)
        assertThat(s.sum).isEqualTo(45.0)
        assertThat(s.mean).isEqualTo(5.0)
        assertThat(s.min).isEqualTo(1.0)
        assertThat(s.max).isEqualTo(9.0)
        assertThat(s.stddev!!).isCloseTo(2.582, offset(1e-3))
    }
}
