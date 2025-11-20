package org.gonexar.operator

import jakarta.persistence.criteria.Expression
import org.gonexar.spatial.SpatialDslContext
import org.gonexar.spatial.SpatialExpr
import org.locationtech.jts.geom.Geometry

/**
 * ST_Buffer(geom, distance)
 */
fun SpatialExpr<Geometry>.buffer(
    dsl: SpatialDslContext<*>,
    distance: Double,
    alias: String? = null
): SpatialExpr<Geometry> {
    val name = alias ?: "${this.name}_buffer_${distance.toString().replace('.', '_')}"
    val cb = dsl.cb
    val buffered: Expression<Geometry> = cb.function("ST_Buffer", Geometry::class.java, this.expr, cb.literal(distance))
    return dsl.register(name, buffered)
}

/**
 * ST_Clip(rast, geom)
 */
fun SpatialExpr<Any>.clip(
    dsl: SpatialDslContext<*>,
    geom: SpatialExpr<Geometry>,
    alias: String? = null
): SpatialExpr<Any> {
    val name = alias ?: "${this.name}_clip_${geom.name}"
    val cb = dsl.cb
    val clipExpr: Expression<Any> = cb.function("ST_Clip", Any::class.java, this.expr, geom.expr)
    return dsl.register(name, clipExpr)
}

/**
 * ST_SummaryStats(raster)  -> returns a composite; we keep it as Any
 */
fun SpatialExpr<Any>.summaryStats(dsl: SpatialDslContext<*>, alias: String? = null): SpatialExpr<Any> {
    val name = alias ?: "${this.name}_stats"
    val cb = dsl.cb
    val statsExpr: Expression<Any> = cb.function("ST_SummaryStats", Any::class.java, this.expr)
    return dsl.register(name, statsExpr)
}

/**
 * ST_Slope(raster, '32BF') -> returns raster of slopes (or raster-like); keep Any
 */
fun SpatialExpr<Any>.slope(dsl: SpatialDslContext<*>, alias: String? = null): SpatialExpr<Any> {
    val name = alias ?: "${this.name}_slope"
    val cb = dsl.cb
    val slopeExpr: Expression<Any> = cb.function("ST_Slope", Any::class.java, this.expr, cb.literal("32BF"))
    return dsl.register(name, slopeExpr)
}

/* ===========================
   WHERE / FILTER HELPERS
   =========================== */

/**
 * Adds ST_Intersects(entity.geom, geom) predicate to the context.
 * entityGeomFieldName: name of geometry column in entity
 */
fun SpatialDslContext<*>.filterIntersects(entityGeomFieldName: String, geom: SpatialExpr<Geometry>) {
    val cb = this.cb
    val entityGeom = this.root.get<Geometry>(entityGeomFieldName)
    val intersects: Expression<Boolean> = cb.function("ST_Intersects", Boolean::class.java, entityGeom, geom.expr)
    this.ctx.addPredicate(cb.isTrue(intersects))
}