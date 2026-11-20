package com.gonexar.type

import com.gonexar.expression.SpatialExpr

data class RasterStatsExpr(
    val min: SpatialExpr<Double?>,
    val max: SpatialExpr<Double?>,
    val mean: SpatialExpr<Double?>,
    val stddev: SpatialExpr<Double?>,
    val count: SpatialExpr<Long>,
    val sum: SpatialExpr<Double?>
)
