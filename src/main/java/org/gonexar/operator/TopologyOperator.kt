package org.gonexar.operator

import org.gonexar.spatial.SpatialDslContext
import org.gonexar.expression.SpatialExpr
import org.locationtech.jts.geom.Geometry

interface TopologyOperator {
    fun SpatialExpr<Geometry>.stIntersects(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_intersects_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_Intersects", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stContains(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_contains_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_Contains", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stWithin(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_within_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_Within", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stCovers(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_covers_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_Covers", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stCoveredBy(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_coveredby_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_CoveredBy", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stTouches(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_touches_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_Touches", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stCrosses(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_crosses_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_Crosses", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stOverlaps(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "st_overlaps_${name}_${other.name}"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_Overlaps", Boolean::class.java,
                expr,
                other.expr
            )
        )

    fun SpatialExpr<Geometry>.stDWithin(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        distance: Double,
        alias: String = "st_dwithin_${name}_${other.name}_$distance"
    ): SpatialExpr<Boolean> =
        dsl.register(
            alias, dsl.cb.function(
                "ST_DWithin", Boolean::class.java,
                expr, other.expr,
                dsl.cb.literal(distance)
            )
        )
}