package org.gonexar.operator

import org.gonexar.spatial.SpatialDslContext
import org.gonexar.spatial.SpatialExpr
import org.gonexar.type.Raster
import org.gonexar.type.RasterStatsExpr
import org.locationtech.jts.geom.Geometry

fun SpatialExpr<Raster>.stValue(
    dsl: SpatialDslContext<*>,
    geom: SpatialExpr<Geometry>,
    alias: String = "st_value_${name}_${geom.name}"
): SpatialExpr<Double?> =
    dsl.register(alias, dsl.cb.function("ST_Value", Double::class.java, expr, geom.expr))

fun SpatialExpr<Raster>.stClip(
    dsl: SpatialDslContext<*>,
    geom: SpatialExpr<Geometry>,
    alias: String = "st_clip_${name}_${geom.name}"
): SpatialExpr<Raster> =
    dsl.register(alias, dsl.cb.function("ST_Clip", Raster::class.java, expr, geom.expr))

fun SpatialExpr<Raster>.stSummaryStats(
    dsl: SpatialDslContext<*>,
    alias: String = "st_stats_${name}"
): RasterStatsExpr {

    val stats = dsl.cb.function("ST_SummaryStats", Any::class.java, expr)

    val min   = dsl.register("${alias}_min",   dsl.cb.function("st_column", Double::class.java, stats, dsl.cb.literal(1)))
    val max   = dsl.register("${alias}_max",   dsl.cb.function("st_column", Double::class.java, stats, dsl.cb.literal(2)))
    val mean  = dsl.register("${alias}_mean",  dsl.cb.function("st_column", Double::class.java, stats, dsl.cb.literal(3)))
    val std   = dsl.register("${alias}_std",   dsl.cb.function("st_column", Double::class.java, stats, dsl.cb.literal(4)))
    val count = dsl.register("${alias}_count", dsl.cb.function("st_column", Long::class.java,   stats, dsl.cb.literal(5)))

    return RasterStatsExpr(min, max, mean, std, count)
}