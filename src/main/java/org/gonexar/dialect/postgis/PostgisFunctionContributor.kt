package org.gonexar.dialect.postgis

import org.gonexar.dialect.DialectCapabilities
import org.gonexar.dialect.SpatialFunctionContributor
import org.gonexar.type.Raster
import org.hibernate.boot.model.FunctionContributions
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.BasicTypeReference
import org.hibernate.type.SqlTypes
import org.hibernate.type.StandardBasicTypes
import org.locationtech.jts.geom.Geometry

/**
 * Registers all PostGIS spatial functions into Hibernate's function registry.
 *
 * This contributor is wired into [GonexarPostgisDialect] and covers:
 * - Core geometry operations (buffer, transform, simplify, envelope …)
 * - Topology predicates (intersects, contains, within, dwithin …)
 * - Metric functions (distance, length, area, perimeter …)
 * - Line/route operations (interpolate, locate, substring …)
 * - Set operations (union, intersection, difference …)
 * - Raster operations (clip, slope, aspect, summaryStats …)
 * - Serialisation (asText, asGeoJSON, asBinary)
 * - Geography cast (`geography` function)
 *
 * ## Adding a new PostGIS function
 *
 * Register it inside [registerFunctions] using the appropriate return type reference:
 *
 * ```kotlin
 * f.register("ST_Snap", StandardSQLFunction("ST_Snap", geometryTypeRef))
 * ```
 *
 * For aggregate functions use [org.hibernate.dialect.function.StandardSqmAggregateFunction] instead.
 *
 * @see GonexarPostgisDialect
 * @see SpatialFunctionContributor
 */
object PostgisFunctionContributor : SpatialFunctionContributor {

    override val capabilities: DialectCapabilities
        get() = DialectCapabilities.POSTGIS

    override fun registerFunctions(functionContributions: FunctionContributions) {
        val f = functionContributions.functionRegistry

        val geometryTypeRef = BasicTypeReference(
            "geometry",
            Geometry::class.java,
            SqlTypes.GEOMETRY
        )

        val rasterTypeRef: BasicTypeReference<Raster> = BasicTypeReference(
            "raster",
            Raster::class.java,
            SqlTypes.VARBINARY
        )

        // ============================
        // CORE GEOMETRY
        // ============================
        f.register("ST_GeometryType", StandardSQLFunction("ST_GeometryType", StandardBasicTypes.STRING))
        f.register("ST_Dimension", StandardSQLFunction("ST_Dimension", StandardBasicTypes.INTEGER))
        f.register("ST_Envelope", StandardSQLFunction("ST_Envelope", geometryTypeRef))
        f.register("ST_Boundary", StandardSQLFunction("ST_Boundary", geometryTypeRef))
        f.register("ST_Buffer", StandardSQLFunction("ST_Buffer", geometryTypeRef))
        f.register("ST_Simplify", StandardSQLFunction("ST_Simplify", geometryTypeRef))
        f.register("ST_Transform", StandardSQLFunction("ST_Transform", geometryTypeRef))
        f.register("ST_SetSRID", StandardSQLFunction("ST_SetSRID", geometryTypeRef))

        // ============================
        // TOPOLOGY PREDICATES
        // ============================
        f.register("ST_Intersects", StandardSQLFunction("ST_Intersects", StandardBasicTypes.BOOLEAN))
        f.register("ST_Contains", StandardSQLFunction("ST_Contains", StandardBasicTypes.BOOLEAN))
        f.register("ST_Within", StandardSQLFunction("ST_Within", StandardBasicTypes.BOOLEAN))
        f.register("ST_Covers", StandardSQLFunction("ST_Covers", StandardBasicTypes.BOOLEAN))
        f.register("ST_CoveredBy", StandardSQLFunction("ST_CoveredBy", StandardBasicTypes.BOOLEAN))
        f.register("ST_Touches", StandardSQLFunction("ST_Touches", StandardBasicTypes.BOOLEAN))
        f.register("ST_Crosses", StandardSQLFunction("ST_Crosses", StandardBasicTypes.BOOLEAN))
        f.register("ST_Overlaps", StandardSQLFunction("ST_Overlaps", StandardBasicTypes.BOOLEAN))
        f.register("ST_DWithin", StandardSQLFunction("ST_DWithin", StandardBasicTypes.BOOLEAN))

        // ============================
        // METRICS
        // ============================
        f.register("ST_Distance", StandardSQLFunction("ST_Distance", StandardBasicTypes.DOUBLE))
        f.register("ST_Length", StandardSQLFunction("ST_Length", StandardBasicTypes.DOUBLE))
        f.register("ST_Area", StandardSQLFunction("ST_Area", StandardBasicTypes.DOUBLE))
        f.register("ST_MaxDistance", StandardSQLFunction("ST_MaxDistance", StandardBasicTypes.DOUBLE))
        f.register("ST_HausdorffDistance", StandardSQLFunction("ST_HausdorffDistance", StandardBasicTypes.DOUBLE))

        // ============================
        // GEOGRAPHY CAST (PostGIS-only)
        // ============================
        f.register("geography", StandardSQLFunction("geography"))
        f.register("geometry", StandardSQLFunction("geometry", geometryTypeRef))

        // ============================
        // LINES / ROUTES
        // ============================
        f.register("ST_LineInterpolatePoint", StandardSQLFunction("ST_LineInterpolatePoint", geometryTypeRef))
        f.register("ST_LineSubstring", StandardSQLFunction("ST_LineSubstring", geometryTypeRef))
        f.register("ST_LineLocatePoint", StandardSQLFunction("ST_LineLocatePoint", StandardBasicTypes.DOUBLE))
        f.register("ST_StartPoint", StandardSQLFunction("ST_StartPoint", geometryTypeRef))
        f.register("ST_EndPoint", StandardSQLFunction("ST_EndPoint", geometryTypeRef))
        f.register("ST_ClosestPoint", StandardSQLFunction("ST_ClosestPoint", geometryTypeRef))
        f.register("ST_Azimuth", StandardSQLFunction("ST_Azimuth", StandardBasicTypes.DOUBLE))
        f.register("ST_LineMerge", StandardSQLFunction("ST_LineMerge", geometryTypeRef))

        // ============================
        // SET OPERATIONS
        // ============================
        f.register("ST_Intersection", StandardSQLFunction("ST_Intersection", geometryTypeRef))
        f.register("ST_Union", StandardSQLFunction("ST_Union", geometryTypeRef))
        f.register("ST_Difference", StandardSQLFunction("ST_Difference", geometryTypeRef))
        f.register("ST_SymDifference", StandardSQLFunction("ST_SymDifference", geometryTypeRef))
        f.register("ST_Collect", StandardSQLFunction("ST_Collect", geometryTypeRef))

        // ============================
        // RASTER (PostGIS-only)
        // ============================
        f.register("ST_Value", StandardSQLFunction("ST_Value", StandardBasicTypes.DOUBLE))
        f.register("ST_NearestValue", StandardSQLFunction("ST_NearestValue", StandardBasicTypes.DOUBLE))
        f.register("ST_Clip", StandardSQLFunction("ST_Clip", rasterTypeRef))
        f.register("ST_SummaryStats", StandardSQLFunction("ST_SummaryStats", StandardBasicTypes.STRING))
        f.register("ST_SummaryStatsAgg", StandardSQLFunction("ST_SummaryStatsAgg", StandardBasicTypes.STRING))
        f.register("ST_MapAlgebraExpr", StandardSQLFunction("ST_MapAlgebraExpr", rasterTypeRef))
        f.register("ST_Reclass", StandardSQLFunction("ST_Reclass", rasterTypeRef))
        f.register("ST_Normalize", StandardSQLFunction("ST_Normalize", rasterTypeRef))
        f.register("ST_Slope", StandardSQLFunction("ST_Slope", rasterTypeRef))
        f.register("ST_Aspect", StandardSQLFunction("ST_Aspect", rasterTypeRef))

        // ============================
        // SERIALISATION
        // ============================
        f.register("ST_AsText", StandardSQLFunction("ST_AsText", StandardBasicTypes.STRING))
        f.register("ST_AsGeoJSON", StandardSQLFunction("ST_AsGeoJSON", StandardBasicTypes.STRING))
        f.register("ST_AsBinary", StandardSQLFunction("ST_AsBinary"))
        f.register("ST_IsValid", StandardSQLFunction("ST_IsValid", StandardBasicTypes.BOOLEAN))
    }
}
