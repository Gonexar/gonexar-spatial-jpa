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
        dsl: SpatialDslContext<*>
    ): SpatialExpr<String> {

        val expr = dsl.cb.function(
            "ST_AsText",
            String::class.java,
            this.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_AsBinary(geom) */
    fun SpatialExpr<Geometry>.asBinary(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<ByteArray> {

        val expr = dsl.cb.function(
            "ST_AsBinary",
            ByteArray::class.java,
            this.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_Buffer(geom, distance) */
    fun SpatialExpr<Geometry>.buffer(
        dsl: SpatialDslContext<*>,
        distance: Double
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Buffer",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(distance)
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_Union(a, b) */
    fun SpatialExpr<Geometry>.union(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Union",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_Intersection(a, b) */
    fun SpatialExpr<Geometry>.intersection(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Intersection",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_Difference(a, b) */
    fun SpatialExpr<Geometry>.difference(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Difference",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_SymDifference(a, b) */
    fun SpatialExpr<Geometry>.symDifference(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_SymDifference",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_LineMerge(geom) */
    fun SpatialExpr<Geometry>.lineMerge(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_LineMerge",
            Geometry::class.java,
            this.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** CAST geometria para geography */
    fun SpatialExpr<Geometry>.asGeography(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<Any> {

        val expr = dsl.cb.function(
            "geography",
            Any::class.java,
            this.expr
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_Length(geography) */
    fun SpatialExpr<Geometry>.lengthGeography(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<Double> {

        val geo = dsl.cb.function("geography", Any::class.java, this.expr)

        val expr = dsl.cb.function(
            "ST_Length",
            Double::class.java,
            geo
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** ST_Distance(a, b) */
    fun SpatialExpr<Geometry>.distance(
        dsl: SpatialDslContext<*>,
        other: SpatialExpr<Geometry>,
        useGeography: Boolean = false
    ): SpatialExpr<Double> {

        val aExpr = if (useGeography) this.asGeography(dsl).expr else this.expr
        val bExpr = if (useGeography) other.asGeography(dsl).expr else other.expr

        val raw = dsl.cb.function(
            "ST_Distance",
            Double::class.java,
            aExpr,
            bExpr
        )

        val converted = dsl.cb.toDouble(raw)
        val alias = dsl.autoAlias(converted)

        return dsl.register(alias, converted)
    }

    /** Raster: ST_Slope(raster) */
    fun SpatialExpr<Any>.slope(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<Any> {

        val expr = dsl.cb.function(
            "ST_Slope",
            Any::class.java,
            this.expr,
            dsl.cb.literal("32BF")
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }

    /** Raster: ST_Value(rast, geom) */
    fun SpatialDslContext<*>.stValue(
        raster: SpatialExpr<Raster>,
        geom: SpatialExpr<Geometry>
    ): SpatialExpr<Double?> {

        val expr = cb.function(
            "ST_Value",
            Double::class.java,
            raster.expr,
            geom.expr
        )

        val alias = autoAlias(expr)
        return register(alias, expr)
    }

    /** Literal Point */
    fun SpatialDslContext<*>.literalPoint(
        coordinate: Coordinate,
        srid: Int = 4326
    ): SpatialExpr<Geometry> {

        val gf = GeometryFactory()
        val p = gf.createPoint(coordinate)
        p.srid = srid

        @Suppress("UNCHECKED_CAST")
        val expr = cb.literal(p) as Expression<Geometry>

        val alias = autoAlias(expr)
        return register(alias, expr)
    }

    /** ST_LineInterpolatePoint */
    fun SpatialExpr<Geometry>.interpolatePoint(
        dsl: SpatialDslContext<*>,
        fraction: Double
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_LineInterpolatePoint",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(fraction)
        )

        val alias = dsl.autoAlias(expr)
        return dsl.register(alias, expr)
    }
}