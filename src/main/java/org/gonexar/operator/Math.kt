package org.gonexar.operator

import org.gonexar.expression.NumericExpr
import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.CriteriaContext
import org.gonexar.spatial.SpatialDslContext
import org.locationtech.jts.geom.Geometry

fun generateSeries(
    dsl: SpatialDslContext<*>,
    start: NumericExpr<Int>,
    stop: NumericExpr<Int>,
    step: NumericExpr<Int> = NumericExpr("", dsl.cb.literal(1))
): NumericExpr<Int> {

    return NumericExpr(
        "generate_series_expresion",
        dsl.cb.function(
            "generate_series",
            Int::class.java,
            start.expr,
            stop.expr,
            step.expr
        )
    )
}

fun SpatialExpr<Geometry>.interpolatePoint(
    ctx: CriteriaContext<*>,
    fraction: NumericExpr<Double>
): SpatialExpr<Geometry> {

    val expr = ctx.cb.function(
        "ST_LineInterpolatePoint",
        Geometry::class.java,
        this.expr,
        fraction.expr
    )

    return SpatialExpr("${name}_interp", expr)
}

