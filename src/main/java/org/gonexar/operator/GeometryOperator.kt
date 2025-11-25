package org.gonexar.operator

import jakarta.persistence.criteria.Expression
import org.gonexar.spatial.SpatialDslContext
import org.gonexar.expression.SpatialExpr
import org.locationtech.jts.geom.Geometry

interface GeometryOperator {

    /** ST_AsText(geom) */
    fun SpatialExpr<Geometry>.stAsText(
        dsl: SpatialDslContext<*, *>
    ): SpatialExpr<String> {

        val expr = dsl.cb.function("ST_AsText", String::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_GeometryType(geom) */
    fun SpatialExpr<Geometry>.stGeometryType(
        dsl: SpatialDslContext<*, *>
    ): SpatialExpr<String> {

        val expr = dsl.cb.function("ST_GeometryType", String::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Dimension(geom) */
    fun SpatialExpr<Geometry>.stDimension(
        dsl: SpatialDslContext<*, *>
    ): SpatialExpr<Int> {

        val expr = dsl.cb.function("ST_Dimension", Int::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Envelope(geom) */
    fun SpatialExpr<Geometry>.stEnvelope(
        dsl: SpatialDslContext<*, *>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function("ST_Envelope", Geometry::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Boundary(geom) */
    fun SpatialExpr<Geometry>.stBoundary(
        dsl: SpatialDslContext<*, *>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function("ST_Boundary", Geometry::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Buffer(geom, distance) */
    fun SpatialExpr<Geometry>.stBuffer(
        dsl: SpatialDslContext<*, *>,
        distance: Double
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Buffer",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(distance)
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Simplify(geom, tolerance) */
    fun SpatialExpr<Geometry>.stSimplify(
        dsl: SpatialDslContext<*, *>,
        tolerance: Double
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Simplify",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(tolerance)
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** Literal user geometry input */
    fun inputGeom(
        dsl: SpatialDslContext<*, *>,
        geometry: Geometry,
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.literal(geometry) as Expression<Geometry>
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Transform(geom, srid) */
    fun SpatialExpr<Geometry>.stTransform(
        dsl: SpatialDslContext<*, *>,
        srid: Int
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Transform",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(srid)
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_SetSRID(geom, srid) */
    fun SpatialExpr<Geometry>.stSetSrid(
        dsl: SpatialDslContext<*, *>,
        srid: Int
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_SetSRID",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(srid)
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Intersection(a, b) */
    fun SpatialExpr<Geometry>.stIntersection(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Intersection",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Union(a, b) */
    fun SpatialExpr<Geometry>.stUnion(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Union",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Difference(a, b) */
    fun SpatialExpr<Geometry>.stDifference(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_Difference",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_SymDifference(a, b) */
    fun SpatialExpr<Geometry>.stSymDifference(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_SymDifference",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_LineMerge(geom) */
    fun SpatialExpr<Geometry>.stLineMerge(
        dsl: SpatialDslContext<*, *>,
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function("ST_LineMerge", Geometry::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_LineInterpolatePoint(geom, fraction) */
    fun SpatialExpr<Geometry>.stLineInterpolatePoint(
        dsl: SpatialDslContext<*, *>,
        fraction: Double
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_LineInterpolatePoint",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(fraction)
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_LineLocatePoint(line, point) */
    fun SpatialExpr<Geometry>.stLineLocatePoint(
        dsl: SpatialDslContext<*, *>,
        point: SpatialExpr<Geometry>
    ): SpatialExpr<Double> {

        val expr = dsl.cb.function(
            "ST_LineLocatePoint",
            Double::class.java,
            this.expr,
            point.expr
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_LineSubstring(geom, start, end) */
    fun SpatialExpr<Geometry>.stLineSubstring(
        dsl: SpatialDslContext<*, *>,
        start: Double,
        end: Double
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_LineSubstring",
            Geometry::class.java,
            this.expr,
            dsl.cb.literal(start),
            dsl.cb.literal(end)
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_StartPoint(geom) */
    fun SpatialExpr<Geometry>.stStartPoint(
        dsl: SpatialDslContext<*, *>,
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function("ST_StartPoint", Geometry::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_EndPoint(geom) */
    fun SpatialExpr<Geometry>.stEndPoint(
        dsl: SpatialDslContext<*, *>,
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function("ST_EndPoint", Geometry::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_ClosestPoint(a, b) */
    fun SpatialExpr<Geometry>.stClosestPoint(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> {

        val expr = dsl.cb.function(
            "ST_ClosestPoint",
            Geometry::class.java,
            this.expr,
            other.expr
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Distance(a, b) */
    fun SpatialExpr<Geometry>.stDistance(
        dsl: SpatialDslContext<*, *>,
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Double> {

        val expr = dsl.cb.function(
            "ST_Distance",
            Double::class.java,
            this.expr,
            other.expr
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Length(geom) */
    fun SpatialExpr<Geometry>.stLength(
        dsl: SpatialDslContext<*, *>,
    ): SpatialExpr<Double> {

        val expr = dsl.cb.function("ST_Length", Double::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Length(geography cast) */
    fun SpatialExpr<Geometry>.stLengthGeography(
        dsl: SpatialDslContext<*, *>,
    ): SpatialExpr<Double> {

        val geog = dsl.cb.function("geography", Any::class.java, this.expr)

        val expr = dsl.cb.function(
            "ST_Length",
            Double::class.java,
            geog
        )

        return dsl.register(dsl.autoAlias(expr), expr)
    }

    /** ST_Area(geom) */
    fun SpatialExpr<Geometry>.stArea(
        dsl: SpatialDslContext<*, *>,
    ): SpatialExpr<Double> {

        val expr = dsl.cb.function("ST_Area", Double::class.java, this.expr)
        return dsl.register(dsl.autoAlias(expr), expr)
    }

    fun SpatialExpr<Geometry>.stPerimeter(
        dsl: SpatialDslContext<*, *>,
        useGeography: Boolean = false,
        useSpheroid: Boolean = true
    ): SpatialExpr<Double> {

        val arg = if (useGeography) {
            // CAST geometry → geography
            dsl.cb.function("geography", Any::class.java, expr)
        } else {
            expr
        }

        val alias = dsl.autoAlias(expr)

        val perimeterExpr =
            if (useGeography)
                dsl.cb.function("ST_Perimeter", Double::class.java, arg, dsl.cb.literal(useSpheroid))
            else
                dsl.cb.function("ST_Perimeter", Double::class.java, arg)

        return dsl.register(alias, perimeterExpr)
    }
}
