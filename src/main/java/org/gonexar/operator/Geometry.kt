package org.gonexar.operator

import org.gonexar.spatial.SpatialDslContext
import org.gonexar.spatial.SpatialExpr
import org.locationtech.jts.geom.Geometry

fun SpatialExpr<Geometry>.stGeometryType(
    dsl: SpatialDslContext<*>,
    alias: String = "${name}_geomtype"
): SpatialExpr<String> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_GeometryType",
            String::class.java, expr
        )
    )

fun SpatialExpr<Geometry>.stDimension(
    dsl: SpatialDslContext<*>,
    alias: String = "${name}_dim"
): SpatialExpr<Int> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Dimension",
            Int::class.java,
            expr
        )
    )

fun SpatialExpr<Geometry>.stEnvelope(
    dsl: SpatialDslContext<*>,
    alias: String = "${name}_env"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Envelope",
            Geometry::class.java, expr
        )
    )

fun SpatialExpr<Geometry>.stBoundary(
    dsl: SpatialDslContext<*>,
    alias: String = "${name}_boundary"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Boundary",
            Geometry::class.java, expr
        )
    )

fun SpatialExpr<Geometry>.stBuffer(
    dsl: SpatialDslContext<*>,
    distance: Double,
    alias: String = "${name}_buffer_$distance"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Buffer",
            Geometry::class.java,
            expr, dsl.cb.literal(distance)
        )
    )

fun SpatialExpr<Geometry>.stSimplify(
    dsl: SpatialDslContext<*>,
    tolerance: Double,
    alias: String = "${name}_simplify_$tolerance"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Simplify",
            Geometry::class.java, expr,
            dsl.cb.literal(tolerance)
        )
    )

fun SpatialExpr<Geometry>.stTransform(
    dsl: SpatialDslContext<*>,
    srid: Int,
    alias: String = "${name}_transform_$srid"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Transform",
            Geometry::class.java,
            expr,
            dsl.cb.literal(srid)
        )
    )

fun SpatialExpr<Geometry>.stSetSrid(
    dsl: SpatialDslContext<*>,
    srid: Int,
    alias: String = "${name}_srid_$srid"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_SetSRID",
            Geometry::class.java,
            expr,
            dsl.cb.literal(srid)
        )
    )

fun SpatialExpr<Geometry>.stIntersection(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "st_intersection_${name}_${other.name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_Intersection", Geometry::class.java,
            expr,
            other.expr
        )
    )

fun SpatialExpr<Geometry>.stUnion(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "st_union_${name}_${other.name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Union",
            Geometry::class.java, expr, other.expr
        )
    )

fun SpatialExpr<Geometry>.stDifference(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "st_diff_${name}_${other.name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_Difference",
            Geometry::class.java, expr, other.expr
        )
    )

fun SpatialExpr<Geometry>.stSymDifference(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "st_symdiff_${name}_${other.name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_SymDifference",
            Geometry::class.java,
            expr, other.expr
        )
    )

fun SpatialExpr<Geometry>.stLineMerge(
    dsl: SpatialDslContext<*>,
    alias: String = "st_linemerge_${name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_LineMerge",
            Geometry::class.java,
            expr
        )
    )

fun SpatialExpr<Geometry>.stLineInterpolatePoint(
    dsl: SpatialDslContext<*>,
    fraction: Double,
    alias: String = "st_linterp_${name}_$fraction"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_LineInterpolatePoint", Geometry::class.java,
            expr,
            dsl.cb.literal(fraction)
        )
    )

fun SpatialExpr<Geometry>.stLineLocatePoint(
    dsl: SpatialDslContext<*>,
    point: SpatialExpr<Geometry>,
    alias: String = "st_llocate_${name}_${point.name}"
): SpatialExpr<Double> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_LineLocatePoint", Double::class.java,
            expr,
            point.expr
        )
    )

fun SpatialExpr<Geometry>.stLineSubstring(
    dsl: SpatialDslContext<*>,
    start: Double,
    end: Double,
    alias: String = "st_lsubstring_${name}_${start}_$end"
): SpatialExpr<Geometry> =
    dsl.register(
        alias,
        dsl.cb.function(
            "ST_LineSubstring", Geometry::class.java,
            expr,
            dsl.cb.literal(start),
            dsl.cb.literal(end)
        )
    )

fun SpatialExpr<Geometry>.stStartPoint(
    dsl: SpatialDslContext<*>,
    alias: String = "st_start_${name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_StartPoint",
            Geometry::class.java,
            expr
        )
    )

fun SpatialExpr<Geometry>.stEndPoint(
    dsl: SpatialDslContext<*>,
    alias: String = "st_end_${name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_EndPoint",
            Geometry::class.java,
            expr
        )
    )

fun SpatialExpr<Geometry>.stClosestPoint(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "st_closest_${name}_${other.name}"
): SpatialExpr<Geometry> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_ClosestPoint", Geometry::class.java,
            expr,
            other.expr
        )
    )

fun SpatialExpr<Geometry>.stDistance(
    dsl: SpatialDslContext<*>,
    other: SpatialExpr<Geometry>,
    alias: String = "st_distance_${name}_${other.name}"
): SpatialExpr<Double> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_Distance", Double::class.java,
            expr,
            other.expr
        )
    )

fun SpatialExpr<Geometry>.stLength(
    dsl: SpatialDslContext<*>,
    alias: String = "st_length_${name}"
): SpatialExpr<Double> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_Length",
            Double::class.java,
            expr
        )
    )

fun SpatialExpr<Geometry>.stLengthGeography(
    dsl: SpatialDslContext<*>,
    alias: String = "st_length_geog_${name}"
): SpatialExpr<Double> {
    val geog = dsl.cb.function("geography", Any::class.java, expr)
    return dsl.register(
        alias, dsl.cb.function(
            "ST_Length",
            Double::class.java,
            geog
        )
    )
}

fun SpatialExpr<Geometry>.stArea(
    dsl: SpatialDslContext<*>,
    alias: String = "st_area_${name}"
): SpatialExpr<Double> =
    dsl.register(
        alias, dsl.cb.function(
            "ST_Area",
            Double::class.java,
            expr
        )
    )

