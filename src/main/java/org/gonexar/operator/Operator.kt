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

fun SpatialExpr<Geometry>.lineMerge(
    dsl: SpatialDslContext<*>,
    alias: String? = null
): SpatialExpr<Geometry> {
    val name = alias ?: "${this.name}_linemerge"
    val expr = dsl.cb.function(
        "ST_LineMerge",
        Geometry::class.java,
        this.expr
    )
    return dsl.register(name, expr)
}

fun SpatialExpr<Geometry>.asGeography(
    dsl: SpatialDslContext<*>,
    alias: String = "${this.name}_geog"
): SpatialExpr<Any> {
    val expr = dsl.cb.function("geography", Any::class.java, this.expr)
    return dsl.register(alias, expr)
}


fun SpatialExpr<Any>.distanceTo(
    dsl: SpatialDslContext<*>,
    otherGeog: SpatialExpr<Any>,
    alias: String = "${this.name}_dist_${otherGeog.name}"
): SpatialExpr<Double> {
    val expr = dsl.cb.function(
        "ST_Distance",
        Double::class.java,
        this.expr,
        otherGeog.expr
    )
    return dsl.register(alias, expr)
}

fun SpatialExpr<Geometry>.intersects(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_intersects_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function(
        "ST_Intersects",
        Boolean::class.java,
        this.expr,
        other.expr
    )
    return dsl.register(alias, expr)
}

fun SpatialExpr<Geometry>.closestPoint(
    dsl: SpatialDslContext<*>,
    to: SpatialExpr<Geometry>,
    alias: String = "${this.name}_closest_point"
): SpatialExpr<Geometry> {
    val expr = dsl.cb.function(
        "ST_ClosestPoint",
        Geometry::class.java,
        this.expr,
        to.expr
    )
    return dsl.register(alias, expr)
}

fun SpatialExpr<Geometry>.intersectionWith(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_intersect_${other.name}"
): SpatialExpr<Geometry> {
    val expr = dsl.cb.function(
        "ST_Intersection",
        Geometry::class.java,
        this.expr,
        other.expr
    )
    return dsl.register(alias, expr)
}

fun SpatialExpr<Geometry>.lengthGeography(
    dsl: SpatialDslContext<*>,
    alias: String = "${this.name}_length"
): SpatialExpr<Double> {
    val geo = SpatialExpr("${this.name}_geo", dsl.cb.function("geography", Any::class.java, this.expr))
    val expr = dsl.cb.function("ST_Length", Double::class.java, geo.expr)
    return dsl.register(alias, expr)
}

fun percentageInside(
    dsl: SpatialDslContext<*>,
    intersectLen: SpatialExpr<Double>,
    totalLen: SpatialExpr<Double>,
    alias: String = "percent_inside"
): SpatialExpr<Double> {

    val cb = dsl.cb

    val raw = cb.prod(
        cb.quot(intersectLen.expr, cb.nullif(totalLen.expr, cb.literal(0.0))),
        cb.literal(100.0)
    )

    val asDouble = cb.toDouble(raw)

    return dsl.register(alias, asDouble)
}


fun bearingAlongRoute(
    dsl: SpatialDslContext<*>,
    mergedRoute: SpatialExpr<Geometry>,
    closestPoint: SpatialExpr<Geometry>,
    alias: String = "route_bearing"
): SpatialExpr<Double> {

    val cb = dsl.cb

    val locate = cb.function(
        "ST_LineLocatePoint",
        Double::class.java,
        mergedRoute.expr,
        closestPoint.expr
    )

    val startFrac = cb.function("GREATEST", Double::class.java,
        cb.diff(locate, cb.literal(0.0001)),
        cb.literal(0.0)
    )

    val endFrac = cb.function("LEAST", Double::class.java,
        cb.sum(locate, cb.literal(0.0001)),
        cb.literal(1.0)
    )

    val smallSegment = cb.function(
        "ST_LineSubstring",
        Geometry::class.java,
        mergedRoute.expr,
        startFrac,
        endFrac
    )

    val bearing = cb.function(
        "ST_Azimuth",
        Double::class.java,
        cb.function("ST_StartPoint", Geometry::class.java, smallSegment),
        cb.function("ST_EndPoint", Geometry::class.java, smallSegment)
    )

    return dsl.register(alias, bearing)
}
