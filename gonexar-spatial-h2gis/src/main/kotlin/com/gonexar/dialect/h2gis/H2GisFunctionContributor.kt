package com.gonexar.dialect.h2gis

import com.gonexar.dialect.DialectCapabilities
import com.gonexar.dialect.SpatialFunctionContributor
import org.hibernate.boot.model.FunctionContributions
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers
import org.hibernate.spatial.JTSGeometryJavaType
import org.hibernate.type.SqlTypes
import org.hibernate.type.StandardBasicTypes

/**
 * Registers H2GIS spatial functions into Hibernate's function registry.
 *
 * H2GIS is an in-memory spatial extension for H2 database. It implements a large
 * subset of the ISO SQL/MM standard (OGC SFS), making it an excellent choice for
 * **integration tests** that need a spatial database without Docker.
 *
 * ## Compatibility notes
 *
 * H2GIS uses the same `ST_*` naming convention as PostGIS for ISO functions.
 * However, several PostGIS-specific features are **not available**:
 *
 * - `geography` type and geodesic metric functions: `geography(...)` and `geometry(...)`
 *   casts are rendered as no-ops, so metric operations run in planar units
 * - `ST_LineLocatePoint` and `ST_HausdorffDistance` (not shipped by H2GIS 2.2)
 * - Raster operations (`ST_Value`, `ST_Clip`, `ST_Slope`, etc.)
 * - `ST_DWithin` (use `ST_Distance(a,b) <= d` instead)
 * - `ST_Transform` requires the H2GIS `spatial_ref_sys` table populated
 *
 * [DialectCapabilities.H2GIS] describes what this dialect supports. The DSL does not check
 * it automatically: calling an unsupported operation fails when H2 executes the query.
 *
 * ## Supported functions
 *
 * | Category       | Functions                                                        |
 * |----------------|------------------------------------------------------------------|
 * | Core geometry  | Envelope, Boundary, Buffer, Simplify, Transform, SetSRID         |
 * | Topology       | Intersects, Contains, Within, Covers, CoveredBy, Touches, Crosses, Overlaps |
 * | Metrics        | Distance, Length, Area                                           |
 * | Lines          | LineInterpolatePoint, LineSubstring, StartPoint, EndPoint, ClosestPoint, Azimuth, LineMerge |
 * | Set operations | Intersection, Union, Difference, SymDifference, Collect          |
 * | Serialisation  | AsText, AsGeoJSON, AsBinary, IsValid                             |
 *
 * @see GonexarH2GisDialect
 * @see SpatialFunctionContributor
 * @see DialectCapabilities.H2GIS
 */
object H2GisFunctionContributor : SpatialFunctionContributor {

    override val capabilities: DialectCapabilities
        get() = DialectCapabilities.H2GIS

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

        // ============================
        // GEOGRAPHY / GEOMETRY CASTS
        // ============================
        // H2GIS has no geography type. The DSL wraps metric operations (buffer, distance,
        // area, lengthGeography) in geography(...)/geometry(...) casts, so both are rendered
        // as no-ops here: the operations still run, in planar units of the input SRID.
        f.patternDescriptorBuilder("geography", "?1")
            .setExactArgumentCount(1)
            .setReturnTypeResolver(StandardFunctionReturnTypeResolvers.useFirstNonNull())
            .register()
        f.patternDescriptorBuilder("geometry", "?1")
            .setExactArgumentCount(1)
            .setReturnTypeResolver(StandardFunctionReturnTypeResolvers.useFirstNonNull())
            .register()

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
        // Note: ST_DWithin is not natively available in H2GIS. Use ST_Distance(a,b) <= d instead.

        // ============================
        // METRICS
        // ============================
        f.register("ST_Distance", StandardSQLFunction("ST_Distance", StandardBasicTypes.DOUBLE))
        f.register("ST_Length", StandardSQLFunction("ST_Length", StandardBasicTypes.DOUBLE))
        f.register("ST_Area", StandardSQLFunction("ST_Area", StandardBasicTypes.DOUBLE))

        // ============================
        // LINES / ROUTES
        // ============================
        registerGeometryFunction("ST_LineInterpolatePoint")
        registerGeometryFunction("ST_LineSubstring")
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
        // SERIALISATION
        // ============================
        f.register("ST_AsText", StandardSQLFunction("ST_AsText", StandardBasicTypes.STRING))
        f.register("ST_AsGeoJSON", StandardSQLFunction("ST_AsGeoJSON", StandardBasicTypes.STRING))
        f.register("ST_AsBinary", StandardSQLFunction("ST_AsBinary", StandardBasicTypes.BINARY))
        f.register("ST_IsValid", StandardSQLFunction("ST_IsValid", StandardBasicTypes.BOOLEAN))
    }
}
