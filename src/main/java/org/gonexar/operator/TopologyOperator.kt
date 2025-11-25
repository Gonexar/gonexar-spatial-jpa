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
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_Intersects",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_Contains(a, b) */
    fun SpatialExpr<Geometry>.stContains(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_Contains",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_Within(a, b) */
    fun SpatialExpr<Geometry>.stWithin(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_Within",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_Covers(a, b) */
    fun SpatialExpr<Geometry>.stCovers(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_Covers",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_CoveredBy(a, b) */
    fun SpatialExpr<Geometry>.stCoveredBy(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_CoveredBy",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_Touches(a, b) */
    fun SpatialExpr<Geometry>.stTouches(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_Touches",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_Crosses(a, b) */
    fun SpatialExpr<Geometry>.stCrosses(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_Crosses",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_Overlaps(a, b) */
    fun SpatialExpr<Geometry>.stOverlaps(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_Overlaps",
            Boolean::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }


    /** ST_DWithin(a, b, distance) */
    fun SpatialExpr<Geometry>.stDWithin(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>,
        distance: Double
    ): SpatialExpr<Boolean> {

        val expr = dsl.cb.function(
            "ST_DWithin",
            Boolean::class.java,
            this.expr,
            other.expr,
            dsl.cb.literal(distance)
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }
}
