package com.gonexar.dialect.postgis

import com.gonexar.dialect.DialectCapabilities
import com.gonexar.dialect.SpatialFunctionContributor
import com.gonexar.type.Raster
import org.hibernate.boot.model.FunctionContributions
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.BasicTypeReference
import org.hibernate.type.SqlTypes
import org.hibernate.type.StandardBasicTypes
import org.hibernate.spatial.JTSGeometryJavaType

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
 * registerGeometryFunction("ST_Snap")
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

        // Resolve the geometry type from the registries instead of a named BasicTypeReference:
        // a reference named "geometry" resolves to an ad-hoc type that Hibernate cannot use
        // as a constructor argument in DTO projections (cb.construct), failing with an NPE.
        val typeConfig = functionContributions.typeConfiguration
        val geometryType = typeConfig.basicTypeRegistry.resolve(
            JTSGeometryJavaType.GEOMETRY_INSTANCE,
            typeConfig.jdbcTypeRegistry.getDescriptor(SqlTypes.GEOMETRY),
        )

        fun registerGeometryFunction(name: String) {
            f.namedDescriptorBuilder(name).setInvariantType(geometryType).register()
        }

        // Same registration key and JDBC code as RasterUserType (contributed by
        // PostgisTypeContributor), so raster function results resolve to that type.
        val rasterTypeRef: BasicTypeReference<Raster> = BasicTypeReference(
            Raster::class.java.typeName,
            Raster::class.java,
            SqlTypes.OTHER
        )

        // ============================
        // CORE GEOMETRY
        // ============================
        f.register("ST_GeometryType", StandardSQLFunction("ST_GeometryType", StandardBasicTypes.STRING))
        f.register("ST_Dimension", StandardSQLFunction("ST_Dimension", StandardBasicTypes.INTEGER))
        registerGeometryFunction("ST_Envelope")
        registerGeometryFunction("ST_Boundary")
        registerGeometryFunction("ST_Buffer")
        registerGeometryFunction("ST_Simplify")
        registerGeometryFunction("ST_Transform")
        registerGeometryFunction("ST_SetSRID")

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
        registerGeometryFunction("geometry")

        // ============================
        // LINES / ROUTES
        // ============================
        registerGeometryFunction("ST_LineInterpolatePoint")
        registerGeometryFunction("ST_LineSubstring")
        f.register("ST_LineLocatePoint", StandardSQLFunction("ST_LineLocatePoint", StandardBasicTypes.DOUBLE))
        registerGeometryFunction("ST_StartPoint")
        registerGeometryFunction("ST_EndPoint")
        registerGeometryFunction("ST_ClosestPoint")
        f.register("ST_Azimuth", StandardSQLFunction("ST_Azimuth", StandardBasicTypes.DOUBLE))
        registerGeometryFunction("ST_LineMerge")

        // ============================
        // SET OPERATIONS
        // ============================
        registerGeometryFunction("ST_Intersection")
        registerGeometryFunction("ST_Union")
        registerGeometryFunction("ST_Difference")
        registerGeometryFunction("ST_SymDifference")
        registerGeometryFunction("ST_Collect")

        // ============================
        // RASTER (PostGIS-only)
        // ============================
        f.register("ST_Value", StandardSQLFunction("ST_Value", StandardBasicTypes.DOUBLE))
        f.register("ST_NearestValue", StandardSQLFunction("ST_NearestValue", StandardBasicTypes.DOUBLE))
        f.register("ST_Clip", StandardSQLFunction("ST_Clip", rasterTypeRef))
        f.register("ST_MapAlgebraExpr", StandardSQLFunction("ST_MapAlgebraExpr", rasterTypeRef))
        f.register("ST_Reclass", StandardSQLFunction("ST_Reclass", rasterTypeRef))
        f.register("ST_Normalize", StandardSQLFunction("ST_Normalize", rasterTypeRef))
        f.register("ST_Slope", StandardSQLFunction("ST_Slope", rasterTypeRef))
        f.register("ST_Aspect", StandardSQLFunction("ST_Aspect", rasterTypeRef))

        // ST_SummaryStats returns a PostgreSQL composite type (summarystats).
        // JPA cannot project composite types directly, so we register one pattern function
        // per field using the PostgreSQL composite-field access syntax: (ST_SummaryStats(?1)).field.
        // This calls ST_SummaryStats once per field, which is the only approach that avoids
        // adding custom SQL functions in the user's database.
        //
        // registerPattern expects BasicType<*>, not BasicTypeReference<*>, so we resolve
        // through the TypeConfiguration that is already available on FunctionContributions.
        val typeRegistry = functionContributions.typeConfiguration.basicTypeRegistry
        val longType   = typeRegistry.resolve(StandardBasicTypes.LONG)
        val doubleType = typeRegistry.resolve(StandardBasicTypes.DOUBLE)

        f.registerPattern("raster_stat_count",  "(ST_SummaryStats(?1)).count",  longType)
        f.registerPattern("raster_stat_sum",    "(ST_SummaryStats(?1)).sum",    doubleType)
        f.registerPattern("raster_stat_mean",   "(ST_SummaryStats(?1)).mean",   doubleType)
        f.registerPattern("raster_stat_stddev", "(ST_SummaryStats(?1)).stddev", doubleType)
        f.registerPattern("raster_stat_min",    "(ST_SummaryStats(?1)).min",    doubleType)
        f.registerPattern("raster_stat_max",    "(ST_SummaryStats(?1)).max",    doubleType)

        // ============================
        // SERIALISATION
        // ============================
        f.register("ST_AsText", StandardSQLFunction("ST_AsText", StandardBasicTypes.STRING))
        f.register("ST_AsGeoJSON", StandardSQLFunction("ST_AsGeoJSON", StandardBasicTypes.STRING))
        f.register("ST_AsBinary", StandardSQLFunction("ST_AsBinary", StandardBasicTypes.BINARY))
        f.register("ST_IsValid", StandardSQLFunction("ST_IsValid", StandardBasicTypes.BOOLEAN))
    }
}
