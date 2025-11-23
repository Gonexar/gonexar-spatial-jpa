package org.gonexar.operator

import jakarta.persistence.criteria.Expression
import org.gonexar.spatial.SpatialDslContext
import org.gonexar.expression.SpatialExpr
import org.gonexar.type.Raster
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory


/**
 * ST_Contains(a, b)  -> true if a contains b
 */
fun SpatialExpr<Geometry>.contains(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_contains_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function("ST_Contains", Boolean::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_Within(a, b) -> true if a is within b
 */
fun SpatialExpr<Geometry>.within(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_within_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function("ST_Within", Boolean::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_Touches(a, b)
 */
fun SpatialExpr<Geometry>.touches(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_touches_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function("ST_Touches", Boolean::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_Crosses(a, b)
 */
fun SpatialExpr<Geometry>.crosses(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_crosses_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function("ST_Crosses", Boolean::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_Overlaps(a, b)
 */
fun SpatialExpr<Geometry>.overlaps(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_overlaps_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function("ST_Overlaps", Boolean::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_Covers(a, b)
 */
fun SpatialExpr<Geometry>.covers(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_covers_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function("ST_Covers", Boolean::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_CoveredBy(a, b)
 */
fun SpatialExpr<Geometry>.coveredBy(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_coveredby_${other.name}"
): SpatialExpr<Boolean> {
    val expr = dsl.cb.function("ST_CoveredBy", Boolean::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_DWithin(a, b, radius)
 *
 * If useGeography = true, wraps both expressions with geography(), making distance computed in meters.
 */
fun SpatialExpr<Geometry>.dWithin(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    radius: Double,
    useGeography: Boolean = false,
    alias: String = "${this.name}_dwithin_${radius}_of_${other.name}"
): SpatialExpr<Boolean> {
    val aExpr = if (useGeography) this.asGeography(dsl).expr else this.expr
    val bExpr = if (useGeography) other.asGeography(dsl).expr else other.expr

    val expr = dsl.cb.function("ST_DWithin", Boolean::class.java, aExpr, bExpr, dsl.cb.literal(radius))
    return dsl.register(alias, expr)
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

/* ============================
   GEOMETRY / NUMERIC OPERATORS
   ============================ */

fun SpatialExpr<Geometry>.distanceTo(
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

/**
 * ST_Distance(a, b)
 *
 * If useGeography = true -> uses geography() for meters measure.
 */
fun SpatialExpr<Geometry>.distance(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    useGeography: Boolean = false,
    alias: String = "${this.name}_dist_${other.name}"
): SpatialExpr<Double> {
    val aExpr = if (useGeography) this.asGeography(dsl).expr else this.expr
    val bExpr = if (useGeography) other.asGeography(dsl).expr else other.expr

    val expr = dsl.cb.function("ST_Distance", Double::class.java, aExpr, bExpr)
    // JPA providers sometimes return Expression<Number> for arithmetic functions; ensure Double via toDouble
    val asDouble = dsl.cb.toDouble(expr)
    return dsl.register(alias, asDouble)
}

/**
 * ST_Buffer(geom, distance)
 */
fun SpatialExpr<Geometry>.buffer(
    dsl: SpatialDslContext<*>,
    distance: Double,
    alias: String = "${this.name}_buffer_${distance.toString().replace('.', '_')}"
): SpatialExpr<Geometry> {
    val expr = dsl.cb.function("ST_Buffer", Geometry::class.java, this.expr, dsl.cb.literal(distance))
    return dsl.register(alias, expr)
}

/**
 * ST_Union(a, b)
 */
fun SpatialExpr<Geometry>.union(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_union_${other.name}"
): SpatialExpr<Geometry> {
    val expr = dsl.cb.function("ST_Union", Geometry::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_Intersection(a, b)
 */
fun SpatialExpr<Geometry>.intersection(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_intersection_${other.name}"
): SpatialExpr<Geometry> {
    val expr = dsl.cb.function("ST_Intersection", Geometry::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_Difference(a, b)
 */
fun SpatialExpr<Geometry>.difference(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_difference_${other.name}"
): SpatialExpr<Geometry> {
    val expr = dsl.cb.function("ST_Difference", Geometry::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}

/**
 * ST_SymDifference(a, b)  (symmetric difference)
 */
fun SpatialExpr<Geometry>.symDifference(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "${this.name}_symdiff_${other.name}"
): SpatialExpr<Geometry> {
    val expr = dsl.cb.function("ST_SymDifference", Geometry::class.java, this.expr, other.expr)
    return dsl.register(alias, expr)
}
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

fun SpatialExpr<Geometry>.interpolatePoint(
    dsl: SpatialDslContext<*>,
    fraction: Double,
    alias: String = "${this.name}_pt_${fraction}"
): SpatialExpr<Geometry> {

    val expr = dsl.cb.function(
        "ST_LineInterpolatePoint",
        Geometry::class.java,
        this.expr,
        dsl.cb.literal(fraction)
    )

    return dsl.register(alias, expr)
}

fun SpatialDslContext<*>.stSetSrid(
    geom: SpatialExpr<Geometry>,
    srid: Int,
    alias: String = "${geom.name}_srid_$srid"
): SpatialExpr<Geometry> {

    val expr = cb.function(
        "ST_SetSRID",
        Geometry::class.java,
        geom.expr,
        cb.literal(srid)
    )

    return register(alias, expr)
}

fun SpatialDslContext<*>.stValue(
    raster: SpatialExpr<Raster>,
    geom: SpatialExpr<Geometry>,
    alias: String = "st_value_${raster.name}_${geom.name}"
): SpatialExpr<Double?> {

    val expr = cb.function(
        "ST_Value",
        Double::class.java,
        raster.expr,
        geom.expr
    )

    return register(alias, expr)
}

fun SpatialDslContext<*>.literalPoint(
    coordinate: Coordinate,
    srid: Int = 4326
): SpatialExpr<Geometry> {
    val gf = GeometryFactory()
    val point = gf.createPoint(coordinate);
    point.srid = srid

    @Suppress("UNCHECKED_CAST")
    val expr = cb.literal(point) as Expression<Geometry>

    return register("literal_point_${coordinate.x}_${coordinate.y}", expr)
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

    val startFrac = cb.function(
        "GREATEST", Double::class.java,
        cb.diff(locate, cb.literal(0.0001)),
        cb.literal(0.0)
    )

    val endFrac = cb.function(
        "LEAST", Double::class.java,
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
