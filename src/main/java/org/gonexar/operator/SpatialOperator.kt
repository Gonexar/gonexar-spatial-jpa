package org.gonexar.operator

import jakarta.persistence.criteria.Expression
import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.SpatialDslContext
import org.gonexar.type.Raster
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory

/**
 * Operadores espaciais NÃO-topológicos.
 *
 * Aqui permanecem apenas funções geométricas ou raster que NÃO fazem parte da topologia
 * (i.e., nada de contains/intersects/within/etc).
 *
 * Toda topologia está em TopologyOperator.
 */
interface SpatialOperator {

    /** ST_AsText(geom) */
    fun SpatialExpr<Geometry>.asText(
        dsl: SpatialDslContext<*>,
        alias: String = "${name}_astext"
    ): SpatialExpr<String> =
        dsl.register(
            alias,
            dsl.cb.function("ST_AsText", String::class.java, expr)
        )

    /** ST_AsBinary(geom) */
    fun SpatialExpr<Geometry>.asBinary(
        dsl: SpatialDslContext<*>,
        alias: String = "${name}_asbinary"
    ): SpatialExpr<ByteArray> =
        dsl.register(
            alias,
            dsl.cb.function("ST_AsBinary", ByteArray::class.java, expr)
        )

    /** ST_Buffer(geom, distance) */
    fun SpatialExpr<Geometry>.buffer(
        dsl: SpatialDslContext<*>,
        distance: Double,
        alias: String = "${name}_buffer_${distance.toString().replace('.', '_')}"
    ): SpatialExpr<Geometry> =
        dsl.register(
            alias,
            dsl.cb.function("ST_Buffer", Geometry::class.java, expr, dsl.cb.literal(distance))
        )

    /** ST_Union(a, b) */
    fun SpatialExpr<Geometry>.union(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "${name}_union_${other.name}"
    ): SpatialExpr<Geometry> =
        dsl.register(
            alias,
            dsl.cb.function("ST_Union", Geometry::class.java, expr, other.expr)
        )

    /** ST_Intersection(a, b) */
    fun SpatialExpr<Geometry>.intersection(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "${name}_intersection_${other.name}"
    ): SpatialExpr<Geometry> =
        dsl.register(
            alias,
            dsl.cb.function("ST_Intersection", Geometry::class.java, expr, other.expr)
        )

    /** ST_Difference(a, b) */
    fun SpatialExpr<Geometry>.difference(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "${name}_difference_${other.name}"
    ): SpatialExpr<Geometry> =
        dsl.register(
            alias,
            dsl.cb.function("ST_Difference", Geometry::class.java, expr, other.expr)
        )

    /** ST_SymDifference(a, b) */
    fun SpatialExpr<Geometry>.symDifference(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        alias: String = "${name}_symdiff_${other.name}"
    ): SpatialExpr<Geometry> =
        dsl.register(
            alias,
            dsl.cb.function("ST_SymDifference", Geometry::class.java, expr, other.expr)
        )

    /** ST_LineMerge(geom) */
    fun SpatialExpr<Geometry>.lineMerge(
        dsl: SpatialDslContext<*>,
        alias: String = "${name}_linemerge"
    ): SpatialExpr<Geometry> =
        dsl.register(
            alias,
            dsl.cb.function("ST_LineMerge", Geometry::class.java, expr)
        )

    /** CAST geometria para geography */
    fun SpatialExpr<Geometry>.asGeography(
        dsl: SpatialDslContext<*>,
        alias: String = "${name}_geog"
    ): SpatialExpr<Any> =
        dsl.register(
            alias,
            dsl.cb.function("geography", Any::class.java, expr)
        )

    /** ST_Length(geography) */
    fun SpatialExpr<Geometry>.lengthGeography(
        dsl: SpatialDslContext<*>,
        alias: String = "${name}_length_geog"
    ): SpatialExpr<Double> {
        val geo = dsl.cb.function("geography", Any::class.java, expr)
        val len = dsl.cb.function("ST_Length", Double::class.java, geo)
        return dsl.register(alias, len)
    }

    /** ST_Distance(a, b) — não topológico, métrico */
    fun SpatialExpr<Geometry>.distance(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        useGeography: Boolean = false,
        alias: String = "${name}_dist_${other.name}"
    ): SpatialExpr<Double> {

        val aExpr = if (useGeography) asGeography(dsl).expr else expr
        val bExpr = if (useGeography) other.asGeography(dsl).expr else other.expr

        val raw = dsl.cb.function("ST_Distance", Double::class.java, aExpr, bExpr)
        return dsl.register(alias, dsl.cb.toDouble(raw))
    }

    /** Raster: ST_Slope(raster) */
    fun SpatialExpr<Any>.slope(
        dsl: SpatialDslContext<*>,
        alias: String = "${name}_slope"
    ): SpatialExpr<Any> =
        dsl.register(
            alias,
            dsl.cb.function("ST_Slope", Any::class.java, expr, dsl.cb.literal("32BF"))
        )

    /** Raster: ST_Value(rast, geom) */
    fun SpatialDslContext<*>.stValue(
        raster: SpatialExpr<Raster>,
        geom: SpatialExpr<Geometry>,
        alias: String = "st_value_${raster.name}_${geom.name}"
    ): SpatialExpr<Double?> =
        register(
            alias,
            cb.function("ST_Value", Double::class.java, raster.expr, geom.expr)
        )

    /** Ponto literal */
    fun SpatialDslContext<*>.literalPoint(
        coordinate: Coordinate,
        srid: Int = 4326
    ): SpatialExpr<Geometry> {
        val gf = GeometryFactory()
        val p = gf.createPoint(coordinate)
        p.srid = srid

        @Suppress("UNCHECKED_CAST")
        val expr = cb.literal(p) as Expression<Geometry>

        return register("literal_point_${coordinate.x}_${coordinate.y}", expr)
    }

    /** ST_LineInterpolatePoint */
    fun SpatialExpr<Geometry>.interpolatePoint(
        dsl: SpatialDslContext<*>,
        fraction: Double,
        alias: String = "${name}_p_${fraction}"
    ): SpatialExpr<Geometry> =
        dsl.register(
            alias,
            dsl.cb.function("ST_LineInterpolatePoint", Geometry::class.java, expr, dsl.cb.literal(fraction))
        )
}
