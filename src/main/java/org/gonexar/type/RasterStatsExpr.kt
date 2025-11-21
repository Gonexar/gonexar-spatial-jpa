package org.gonexar.type

import org.gonexar.spatial.SpatialExpr

data class RasterStatsExpr(
    val min: SpatialExpr<Double?>,
    val max: SpatialExpr<Double?>,
    val mean: SpatialExpr<Double?>,
    val stddev: SpatialExpr<Double?>,
    val count: SpatialExpr<Long>
)
