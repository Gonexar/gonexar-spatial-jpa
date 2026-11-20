package com.gonexar.operator

import com.gonexar.spatial.SpatialDslContext
import com.gonexar.expression.SpatialExpr
import com.gonexar.type.Raster
import com.gonexar.type.RasterStatsExpr
import org.locationtech.jts.geom.Geometry

interface MatrixOperator {

    fun SpatialExpr<Raster>.stValue(
        dsl: SpatialDslContext<*, *>,
        geom: SpatialExpr<Geometry>
    ): SpatialExpr<Double?> {
        val value = dsl.cb.function("ST_Value", Double::class.java, expr, geom.expr)
        return dsl.register(dsl.autoAlias(value), value)
    }

    fun SpatialExpr<Raster>.stClip(
        dsl: SpatialDslContext<*, *>,
        geom: SpatialExpr<Geometry>
    ): SpatialExpr<Raster> {
        val clipped = dsl.cb.function("ST_Clip", Raster::class.java, expr, geom.expr)
        return dsl.register(dsl.autoAlias(clipped), clipped)
    }

    /**
     * Projects each field of the PostGIS `summarystats` composite type as a separate
     * SELECT expression. Generates `(ST_SummaryStats(rast)).field` per stat — the only
     * approach compatible with JPA Criteria API without custom DB functions.
     *
     * Each stat is an independent expression, so ST_SummaryStats is called once per field
     * in the generated SQL. Use this in SELECT projections only; avoid calling it in WHERE.
     */
    fun SpatialExpr<Raster>.stSummaryStats(
        dsl: SpatialDslContext<*, *>
    ): RasterStatsExpr {
        val alias = dsl.autoAlias(expr)

        val count  = dsl.register("${alias}_count",  dsl.cb.function("raster_stat_count",  Long::class.java,   expr))
        val sum    = dsl.register("${alias}_sum",    dsl.cb.function("raster_stat_sum",    Double::class.java, expr))
        val mean   = dsl.register("${alias}_mean",   dsl.cb.function("raster_stat_mean",   Double::class.java, expr))
        val stddev = dsl.register("${alias}_stddev", dsl.cb.function("raster_stat_stddev", Double::class.java, expr))
        val min    = dsl.register("${alias}_min",    dsl.cb.function("raster_stat_min",    Double::class.java, expr))
        val max    = dsl.register("${alias}_max",    dsl.cb.function("raster_stat_max",    Double::class.java, expr))

        return RasterStatsExpr(min, max, mean, stddev, count, sum)
    }
}
