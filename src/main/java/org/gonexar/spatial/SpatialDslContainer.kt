package org.gonexar.spatial

import org.gonexar.expression.NumericExpr
import org.gonexar.expression.SpatialExpr
import org.gonexar.operator.GeometryOperator
import org.gonexar.operator.MathOperator
import org.gonexar.operator.MatrixOperator
import org.gonexar.operator.SpatialOperator
import org.gonexar.operator.TemporalOperator
import org.gonexar.operator.TopologyOperator
import org.gonexar.type.Raster
import org.gonexar.type.RasterStatsExpr
import org.locationtech.jts.geom.Geometry

class SpatialDslContainer<E : Any, R : Any>(
    ctx: CriteriaDslContext<E, R>
) : SpatialDslContext<E, R>(ctx),
    GeometryOperator,
    MathOperator,
    MatrixOperator,
    SpatialOperator,
    TopologyOperator,
    TemporalOperator {

    fun SpatialExpr<Geometry>.buffer(
        distance: Double
    ): SpatialExpr<Geometry> = this.stBuffer(dsl, distance)

    fun toGeometryExpr(
        geometry: Geometry
    ): SpatialExpr<Geometry> = this.inputGeom(dsl, geometry)

    fun SpatialExpr<Geometry>.intersects(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stIntersects(dsl, other)

    /** ST_Contains(a, b) */
    fun SpatialExpr<Geometry>.contains(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stContains(dsl, other)

    /** ST_Within(a, b) */
    fun SpatialExpr<Geometry>.within(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stWithin(dsl, other)

    /** ST_Covers(a, b) */
    fun SpatialExpr<Geometry>.covers(
        other: SpatialExpr<Geometry>,
    ): SpatialExpr<Boolean> = this.stCovers(dsl, other)

    /** ST_CoveredBy(a, b) */
    fun SpatialExpr<Geometry>.coveredBy(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stCoveredBy(dsl, other)

    /** ST_Touches(a, b) */
    fun SpatialExpr<Geometry>.touches(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stTouches(dsl, other)

    /** ST_Crosses(a, b) */
    fun SpatialExpr<Geometry>.crosses(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stCrosses(dsl, other)

    /** ST_Overlaps(a, b) */
    fun SpatialExpr<Geometry>.overlaps(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stOverlaps(dsl, other)

    /** ST_DWithin(a, b, distance) */
    fun SpatialExpr<Geometry>.dWithin(
        other: SpatialExpr<Geometry>,
        distance: Double
    ): SpatialExpr<Boolean> = this.stDWithin(dsl, other, distance)

    /** YEAR(expr) */
    fun SpatialExpr<Any>.year(): SpatialExpr<Int> = this.year(dsl)

    /**  MONTH(expr) */
    fun SpatialExpr<Any>.month(
    ): SpatialExpr<Int> = this.month(dsl)

    /** DAY(expr) */
    fun SpatialExpr<Any>.day(
    ): SpatialExpr<Int> = this.day(dsl)

    /** ST_AsText(geom) */
    fun SpatialExpr<Geometry>.asText(): SpatialExpr<String> = this.asText(dsl)

    /** ST_AsBinary(geom) */
    fun SpatialExpr<Geometry>.asBinary(): SpatialExpr<ByteArray> = this.asBinary(dsl)

    /** ST_Simplify(geom, tolerance) */
    fun SpatialExpr<Geometry>.simplify(
        tolerance: Double
    ): SpatialExpr<Geometry> = this.stSimplify(dsl, tolerance)

    /** ST_Transform(geom, srid) */
    fun SpatialExpr<Geometry>.transform(
        srid: Int
    ): SpatialExpr<Geometry> = this.stTransform(dsl, srid)

    /** ST_SetSRID(geom, srid) */
    fun SpatialExpr<Geometry>.setSrid(
        srid: Int
    ): SpatialExpr<Geometry> = this.stSetSrid(dsl, srid)

    /** ST_Intersection(a, b) */
    fun SpatialExpr<Geometry>.intersection(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> = this.stIntersection(dsl, other)

    /** ST_Union(a, b) */
    fun SpatialExpr<Geometry>.union(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> = this.stUnion(dsl, other)

    /** ST_Difference(a, b) */
    fun SpatialExpr<Geometry>.difference(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> = this.stDifference(dsl, other)

    /** ST_SymDifference(a, b) */
    fun SpatialExpr<Geometry>.symDifference(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> = this.stSymDifference(dsl, other)

    /** ST_LineMerge(geom) */
    fun SpatialExpr<Geometry>.lineMerge(): SpatialExpr<Geometry> = this.stLineMerge(dsl)

    /** ST_LineInterpolatePoint(geom, fraction) */
    fun SpatialExpr<Geometry>.stLineInterpolatePoint(
        fraction: Double
    ): SpatialExpr<Geometry> = this.stLineInterpolatePoint(dsl, fraction)

    /** ST_LineLocatePoint(line, point) */
    fun SpatialExpr<Geometry>.lineLocatePoint(
        point: SpatialExpr<Geometry>
    ): SpatialExpr<Double> = this.stLineLocatePoint(dsl, point)

    /** ST_LineSubstring(geom, start, end) */
    fun SpatialExpr<Geometry>.lineSubstring(
        start: Double,
        end: Double
    ): SpatialExpr<Geometry> = this.stLineSubstring(dsl, start, end)

    /** ST_StartPoint(geom) */
    fun SpatialExpr<Geometry>.startPoint(
    ): SpatialExpr<Geometry> = this.stStartPoint(dsl)

    /** ST_EndPoint(geom) */
    fun SpatialExpr<Geometry>.endPoint(
    ): SpatialExpr<Geometry> = this.stEndPoint(dsl)

    /** ST_ClosestPoint(a, b) */
    fun SpatialExpr<Geometry>.closestPoint(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> = this.stClosestPoint(dsl, other)

    /** ST_Distance(a, b) */
    fun SpatialExpr<Geometry>.distance(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Double> = this.stDistance(dsl, other)

    /** ST_Length(geom) */
    fun SpatialExpr<Geometry>.length(): SpatialExpr<Double> = this.stLength(dsl)

    /** ST_Length(geography cast) */
    fun SpatialExpr<Geometry>.lengthGeography(
    ): SpatialExpr<Double> = this.stLengthGeography(dsl)

    /** ST_Area(geom) */
    fun SpatialExpr<Geometry>.area(
    ): SpatialExpr<Double> = this.stArea(dsl)

    fun SpatialExpr<Geometry>.stPerimeter(
        useGeography: Boolean = false,
        useSpheroid: Boolean = true,
    ): SpatialExpr<Double> = this.stPerimeter(dsl, useGeography, useSpheroid)

    fun SpatialExpr<Raster>.value(
        geom: SpatialExpr<Geometry>
    ): SpatialExpr<Double?> = this.stValue(dsl, geom)

    fun SpatialExpr<Raster>.stSummaryStats(
    ): RasterStatsExpr = this.stSummaryStats(dsl)

    /** generate_series(start, stop, step) */
    fun generateSeries(
        start: NumericExpr<Int>,
        stop: NumericExpr<Int>,
        step: NumericExpr<Int> = NumericExpr("lit_1", dsl.cb.literal(1))
    ): NumericExpr<Int> = generateSeries(dsl, start, stop, step)

    /** ST_LineInterpolatePoint(geom, fraction) */
    fun SpatialExpr<Geometry>.interpolatePoint(
        fraction: NumericExpr<Double>
    ): SpatialExpr<Geometry> = this.interpolatePoint(dsl, fraction)
}