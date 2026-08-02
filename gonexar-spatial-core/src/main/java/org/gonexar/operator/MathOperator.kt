package org.gonexar.operator

import org.gonexar.expression.NumericExpr
import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.SpatialDslContext
import org.locationtech.jts.geom.Geometry

interface MathOperator {

    /** generate_series(start, stop, step) */
    fun generateSeries(
        dsl: SpatialDslContext<*, *>,
        start: NumericExpr<Int>,
        stop: NumericExpr<Int>,
        step: NumericExpr<Int> = NumericExpr("lit_1", dsl.cb.literal(1))
    ): NumericExpr<Int> {

        val expr = dsl.cb.function(
            "generate_series",
            Int::class.java,
            start.expr,
            stop.expr,
            step.expr
        )

        val alias = dsl.autoAlias(expr)

        return NumericExpr(alias, expr)
    }

    /** ST_LineInterpolatePoint(geom, fraction) */
    fun SpatialExpr<Geometry>.interpolatePoint(
        dsl: SpatialDslContext<*, *>,
        fraction: NumericExpr<Double>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_LineInterpolatePoint",
            Geometry::class.java,
            this.expr,
            fraction.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }

    fun SpatialDslContext<*, *>.percentageInsideIntersection(
        intersectLength: SpatialExpr<Double>,
        totalLength: SpatialExpr<Double>,
        alias: String = "percent_inside"
    ): SpatialExpr<Double> {

        val cb = this.cb

        val raw = cb.prod(
            cb.quot(
                intersectLength.expr,
                cb.nullif(totalLength.expr, cb.literal(0.0))
            ),
            cb.literal(100.0)
        )

        val asDouble = cb.toDouble(raw)

        return register(alias, asDouble)
    }

    fun SpatialDslContext<*, *>.bearingAlongRoute(
        route: SpatialExpr<Geometry>,
        closestPoint: SpatialExpr<Geometry>,
        alias: String = "route_bearing"
    ): SpatialExpr<Double> {

        val cb = this.cb

        val locate = cb.function(
            "ST_LineLocatePoint",
            Double::class.java,
            route.expr,
            closestPoint.expr
        )

        val startFrac = cb.function(
            "GREATEST",
            Double::class.java,
            cb.diff(locate, cb.literal(0.0001)),
            cb.literal(0.0)
        )

        val endFrac = cb.function(
            "LEAST",
            Double::class.java,
            cb.sum(locate, cb.literal(0.0001)),
            cb.literal(1.0)
        )

        val smallSegment = cb.function(
            "ST_LineSubstring",
            Geometry::class.java,
            route.expr,
            startFrac,
            endFrac
        )

        val bearing = cb.function(
            "ST_Azimuth",
            Double::class.java,
            cb.function("ST_StartPoint", Geometry::class.java, smallSegment),
            cb.function("ST_EndPoint", Geometry::class.java, smallSegment)
        )

        return register(alias, bearing)
    }
}

