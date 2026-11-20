package com.gonexar.dialect.h2gis

import com.gonexar.dialect.h2gis.H2GisTestDatabase.geom
import com.gonexar.dialect.h2gis.H2GisTestDatabase.inTransaction
import com.gonexar.expression.SpatialExpr
import com.gonexar.query.intersects
import com.gonexar.repository.GonexarSpatialRepository
import com.gonexar.spatial.SpatialContext
import com.gonexar.spatial.SpatialDslContainer
import com.gonexar.spatial.query
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Geometry

/**
 * End-to-end tests of the spatial DSL against H2 + H2GIS: every query is built
 * with the DSL, translated by Hibernate with [GonexarH2GisDialect] and executed.
 */
class H2GisSpatialDslTest {

    private fun <R : Any> areas(
        resultClass: Class<R>,
        block: SpatialDslContainer<Area, R>.() -> Unit,
    ): List<R> = inTransaction { em -> query(SpatialContext(em, Area::class.java), resultClass, block) }

    private fun <R : Any> routes(
        resultClass: Class<R>,
        block: SpatialDslContainer<Route, R>.() -> Unit,
    ): List<R> = inTransaction { em -> query(SpatialContext(em, Route::class.java), resultClass, block) }

    private fun SpatialDslContainer<*, *>.nameColumn() = entity.get<String>("name")

    private fun namesWhere(predicate: SpatialDslContainer<Area, Area>.() -> SpatialExpr<Boolean>): List<String> =
        areas(Area::class.java) {
            where { _, _ -> predicate().toPredicate() }
        }.map { it.name }.sorted()

    // ---------------------------------------------------------------------
    // Topology predicates
    // ---------------------------------------------------------------------

    @Test
    fun `intersects filters areas touching the input geometry`() {
        val names = namesWhere {
            toGeometryExpr(geom("POINT(10 5)")).intersects(geomColumn("polygon"))
        }
        assertThat(names).containsExactly("neighbor", "square")
    }

    @Test
    fun `contains and within are inverse predicates`() {
        val inner = geom("POLYGON((2 2, 4 2, 4 4, 2 4, 2 2))")

        assertThat(namesWhere { geomColumn("polygon").contains(toGeometryExpr(inner)) })
            .containsExactly("inner", "square")
        assertThat(namesWhere { toGeometryExpr(inner).within(geomColumn("polygon")) })
            .containsExactly("inner", "square")
    }

    @Test
    fun `covers and coveredBy include boundary points`() {
        val corner = geom("POINT(0 0)")

        assertThat(namesWhere { geomColumn("polygon").covers(toGeometryExpr(corner)) })
            .containsExactly("square")
        assertThat(namesWhere { toGeometryExpr(corner).coveredBy(geomColumn("polygon")) })
            .containsExactly("square")
    }

    @Test
    fun `touches finds areas sharing only a boundary`() {
        val square = geom("POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))")
        assertThat(namesWhere { geomColumn("polygon").touches(toGeometryExpr(square)) })
            .containsExactly("neighbor")
    }

    @Test
    fun `crosses finds areas a line passes through`() {
        val line = geom("LINESTRING(-5 5, 5 5)")
        assertThat(namesWhere { toGeometryExpr(line).crosses(geomColumn("polygon")) })
            .containsExactly("square")
    }

    @Test
    fun `overlaps finds partially overlapping areas`() {
        val shifted = geom("POLYGON((5 5, 15 5, 15 15, 5 15, 5 5))")
        assertThat(namesWhere { geomColumn("polygon").overlaps(toGeometryExpr(shifted)) })
            .containsExactly("neighbor", "square")
    }

    @Test
    fun `repository intersects helper returns matching entities`() {
        val names = inTransaction { em ->
            AreaRepository(em).intersects(geom("POINT(3 3)")).map { it.name }.sorted()
        }
        assertThat(names).containsExactly("inner", "square")
    }

    // ---------------------------------------------------------------------
    // Projections
    // ---------------------------------------------------------------------

    @Test
    fun `asText projects geometries as WKT into a DTO`() {
        val rows = areas(NameAndText::class.java) {
            whereEq(nameColumn(), "inner")
            select<NameAndText> {
                expr(nameColumn())
                field(geomColumn("polygon").asText())
            }
        }
        assertThat(rows).containsExactly(NameAndText("inner", "POLYGON ((2 2, 4 2, 4 4, 2 4, 2 2))"))
    }

    @Test
    fun `planar length is projected as a number`() {
        val rows = routes(NameAndNumber::class.java) {
            whereEq(entity.get<String>("name"), "straight")
            select<NameAndNumber> {
                expr(entity.get<String>("name"))
                field(geomColumn("path").length())
            }
        }
        assertThat(rows.single().value).isCloseTo(10.0, within(1e-9))
    }

    @Test
    fun `set operations return geometries`() {
        val other = geom("POLYGON((5 0, 15 0, 15 10, 5 10, 5 0))")

        fun areaOf(op: SpatialDslContainer<Area, NameAndGeometry>.(SpatialExpr<Geometry>, SpatialExpr<Geometry>) -> SpatialExpr<Geometry>) =
            areas(NameAndGeometry::class.java) {
                whereEq(nameColumn(), "square")
                select<NameAndGeometry> {
                    expr(nameColumn())
                    field(op(geomColumn("polygon"), toGeometryExpr(other)))
                }
            }.single().geometry.area

        assertThat(areaOf { a, b -> a.intersection(b) }).isCloseTo(50.0, within(1e-9))
        assertThat(areaOf { a, b -> a.union(b) }).isCloseTo(150.0, within(1e-9))
        assertThat(areaOf { a, b -> a.difference(b) }).isCloseTo(50.0, within(1e-9))
        assertThat(areaOf { a, b -> a.symDifference(b) }).isCloseTo(100.0, within(1e-9))
    }

    @Test
    fun `line referencing operators work on routes`() {
        val rows = routes(NameAndGeometry::class.java) {
            whereEq(entity.get<String>("name"), "straight")
            select<NameAndGeometry> {
                expr(entity.get<String>("name"))
                field(geomColumn("path").stLineInterpolatePoint(0.5))
            }
        }
        assertThat(rows.single().geometry.toText()).isEqualTo("POINT (5 20)")

        val endpoints = routes(NameAndText::class.java) {
            whereEq(entity.get<String>("name"), "straight")
            select<NameAndText> {
                expr(entity.get<String>("name"))
                field(
                    geomColumn("path").lineSubstring(0.0, 0.5).endPoint().asText()
                )
            }
        }
        assertThat(endpoints.single().wkt).isEqualTo("POINT (5 20)")
    }

    @Test
    fun `metric operators run in planar units because geography casts are no-ops`() {
        val area = areas(NameAndNumber::class.java) {
            whereEq(nameColumn(), "square")
            select<NameAndNumber> {
                expr(nameColumn())
                field(geomColumn("polygon").area())
            }
        }
        assertThat(area.single().value).isCloseTo(100.0, within(1e-9))

        val distance = areas(NameAndNumber::class.java) {
            whereEq(nameColumn(), "far")
            select<NameAndNumber> {
                expr(nameColumn())
                field(geomColumn("polygon").distance(toGeometryExpr(geom("POINT(100 90)"))))
            }
        }
        assertThat(distance.single().value).isCloseTo(10.0, within(1e-9))

        val length = routes(NameAndNumber::class.java) {
            whereEq(entity.get<String>("name"), "straight")
            select<NameAndNumber> {
                expr(entity.get<String>("name"))
                field(geomColumn("path").lengthGeography())
            }
        }
        assertThat(length.single().value).isCloseTo(10.0, within(1e-9))

        val buffered = namesWhere {
            toGeometryExpr(geom("POINT(-1 5)")).buffer(1.5).intersects(geomColumn("polygon"))
        }
        assertThat(buffered).containsExactly("square")
    }

    @Test
    fun `closestPoint and startPoint return points`() {
        val rows = routes(NameAndGeometry::class.java) {
            whereEq(entity.get<String>("name"), "straight")
            select<NameAndGeometry> {
                expr(entity.get<String>("name"))
                field(geomColumn("path").closestPoint(toGeometryExpr(geom("POINT(3 0)"))))
            }
        }
        assertThat(rows.single().geometry.toText()).isEqualTo("POINT (3 20)")

        val start = routes(NameAndGeometry::class.java) {
            whereEq(entity.get<String>("name"), "diagonal")
            select<NameAndGeometry> {
                expr(entity.get<String>("name"))
                field(geomColumn("path").startPoint())
            }
        }
        assertThat(start.single().geometry.toText()).isEqualTo("POINT (0 0)")
    }

    @Test
    fun `simplify and setSrid keep geometries usable`() {
        val rows = areas(NameAndGeometry::class.java) {
            whereEq(nameColumn(), "square")
            select<NameAndGeometry> {
                expr(nameColumn())
                field(geomColumn("polygon").simplify(0.5).setSrid(3857))
            }
        }
        val g = rows.single().geometry
        assertThat(g.area).isCloseTo(100.0, within(1e-9))
        assertThat(g.srid).isEqualTo(3857)
    }

    @Test
    fun `temporal operators extract date parts`() {
        val rows = areas(NameAndDate::class.java) {
            whereEq(nameColumn(), "square")
            val date = SpatialExpr<Any>("surveyed", entity.get("surveyedAt"))
            select<NameAndDate> {
                expr(nameColumn())
                field(date.year())
                field(date.month())
                field(date.day())
            }
        }
        assertThat(rows).containsExactly(NameAndDate("square", 2024, 3, 15))
    }

    // ---------------------------------------------------------------------
    // Query plumbing
    // ---------------------------------------------------------------------

    @Test
    fun `query without projection returns entities`() {
        val all = areas(Area::class.java) {}
        assertThat(all.map { it.name }).containsExactlyInAnyOrder("square", "inner", "neighbor", "far")
    }

    @Test
    fun `reified query overload delegates to the class based one`() {
        val names = inTransaction { em ->
            query<Area, Area>(SpatialContext(em, Area::class.java)) {
                where { root, cb -> cb.equal(root.get<Long>("id"), 2L) }
            }.map { it.name }
        }
        assertThat(names).containsExactly("inner")
    }

    @Test
    fun `comparison helpers build where clauses`() {
        fun ids(block: SpatialDslContainer<Area, Area>.() -> Unit) =
            areas(Area::class.java, block).map { it.id }.sorted()

        assertThat(ids { whereGt(entity.get<Long>("id"), 2L) }).containsExactly(3L, 4L)
        assertThat(ids { whereGte(entity.get<Long>("id"), 2L) }).containsExactly(2L, 3L, 4L)
        assertThat(ids { whereLt(entity.get<Long>("id"), 2L) }).containsExactly(1L)
        assertThat(ids { whereLte(entity.get<Long>("id"), 2L) }).containsExactly(1L, 2L)
        assertThat(ids { whereNeq(entity.get<Long>("id"), 2L) }).containsExactly(1L, 3L, 4L)

        assertThat(ids { where(entity.get<Long>("id") gt 3L) }).containsExactly(4L)
        assertThat(ids { where(entity.get<Long>("id") gte 3L) }).containsExactly(3L, 4L)
        assertThat(ids { where(entity.get<Long>("id") lt 2L) }).containsExactly(1L)
        assertThat(ids { where(entity.get<Long>("id") lte 2L) }).containsExactly(1L, 2L)
        assertThat(ids { where(entity.get<Long>("id") eq 3L) }).containsExactly(3L)
        assertThat(ids { where(entity.get<Long>("id") neq 3L) }).containsExactly(1L, 2L, 4L)
    }

    @Test
    fun `boolean expressions can be used as predicates and projections`() {
        val point = geom("POINT(3 3)")
        val rows = areas(NameAndFlag::class.java) {
            val hit = geomColumn("polygon").intersects(toGeometryExpr(point))
            where(hit.asPredicate())
            select<NameAndFlag> {
                expr(nameColumn())
                field(hit)
            }
        }
        assertThat(rows.map { it.name }).containsExactlyInAnyOrder("square", "inner")
        assertThat(rows.map { it.flag }).containsOnly(true)
    }

    @Test
    fun `nested dto projections build constructor arguments in order`() {
        val rows = areas(AreaSummary::class.java) {
            whereEq(nameColumn(), "inner")
            select<AreaSummary> {
                expr(nameColumn())
                dto<NameAndText> {
                    expr(nameColumn())
                    field(geomColumn("polygon").asText())
                }
            }
        }
        assertThat(rows).containsExactly(
            AreaSummary("inner", NameAndText("inner", "POLYGON ((2 2, 4 2, 4 4, 2 4, 2 2))"))
        )
    }

    private class AreaRepository(em: EntityManager) : GonexarSpatialRepository<Area> {
        override val ctx = SpatialContext(em, Area::class.java)
    }
}

data class AreaSummary(val name: String, val detail: NameAndText)
