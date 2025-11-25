package org.gonexar.operator

import org.gonexar.spatial.SpatialDslContext
import org.gonexar.expression.SpatialExpr
import org.locationtech.jts.geom.Geometry

/**
 * Operadores Topológicos do PostGIS.
 *
 * Estes operadores trabalham exclusivamente com relações espaciais
 * entre duas geometrias.
 *
 * Eles são integrados ao DSL através do SpatialDslContainer.
 */
interface TopologyOperator {

    /** ST_Intersects(a, b) */
    fun SpatialExpr<Geometry>.stIntersects(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_intersects_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_Intersects", Boolean::class.java, expr, other.expr)
        )

    /** ST_Contains(a, b) */
    fun SpatialExpr<Geometry>.stContains(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_contains_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_Contains", Boolean::class.java, expr, other.expr)
        )

    /** ST_Within(a, b) */
    fun SpatialExpr<Geometry>.stWithin(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_within_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_Within", Boolean::class.java, expr, other.expr)
        )

    /** ST_Covers(a, b) */
    fun SpatialExpr<Geometry>.stCovers(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_covers_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_Covers", Boolean::class.java, expr, other.expr)
        )

    /** ST_CoveredBy(a, b) */
    fun SpatialExpr<Geometry>.stCoveredBy(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_coveredby_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_CoveredBy", Boolean::class.java, expr, other.expr)
        )

    /** ST_Touches(a, b) */
    fun SpatialExpr<Geometry>.stTouches(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_touches_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_Touches", Boolean::class.java, expr, other.expr)
        )

    /** ST_Crosses(a, b) */
    fun SpatialExpr<Geometry>.stCrosses(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_crosses_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_Crosses", Boolean::class.java, expr, other.expr)
        )

    /** ST_Overlaps(a, b) */
    fun SpatialExpr<Geometry>.stOverlaps(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_overlaps_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_Overlaps", Boolean::class.java, expr, other.expr)
        )

    /** ST_DWithin(a, b, distance) */
    fun SpatialExpr<Geometry>.stDWithin(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        distance: Double,
        alias: String = "st_dwithin_${name}_${other.name}_$distance"
    ): SpatialExpr<Boolean> =
        dsl.register(alias,
            dsl.cb.function("ST_DWithin", Boolean::class.java,
                expr, other.expr, dsl.cb.literal(distance)
            )
        )
}
