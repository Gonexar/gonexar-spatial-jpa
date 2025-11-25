package org.gonexar.operator

import org.gonexar.expression.NumericExpr
import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.SpatialDslContext
import org.locationtech.jts.geom.Geometry

interface MathOperator {

    /** generate_series(start, stop, step) */
    fun generateSeries(
        dsl: SpatialDslContext<*>,
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
        dsl: SpatialDslContext<*>,
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
}

