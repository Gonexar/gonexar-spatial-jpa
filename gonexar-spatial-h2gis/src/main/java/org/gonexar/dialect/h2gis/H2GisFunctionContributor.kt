package org.gonexar.dialect.h2gis

import org.gonexar.dialect.DialectCapabilities
import org.gonexar.dialect.SpatialFunctionContributor
import org.hibernate.boot.model.FunctionContributions
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.BasicTypeReference
import org.hibernate.type.SqlTypes
import org.hibernate.type.StandardBasicTypes
import org.locationtech.jts.geom.Geometry

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
 * - `geography` type and geodesic metric functions
 * - Raster operations (`ST_Value`, `ST_Clip`, `ST_Slope`, etc.)
 * - `ST_DWithin` (use `ST_Distance(a,b) <= d` instead)
 * - `ST_Transform` requires the H2GIS `spatial_ref_sys` table populated
 *
 * The DSL guards dialect-specific operations via [DialectCapabilities.H2GIS].
 *
 * ## Supported functions
 *
 * | Category       | Functions                                                        |
 * |----------------|------------------------------------------------------------------|
 * | Core geometry  | Envelope, Boundary, Buffer, Simplify, Transform, SetSRID         |
 * | Topology       | Intersects, Contains, Within, Covers, CoveredBy, Touches, Crosses, Overlaps |
 * | Metrics        | Distance, Length, Area, HausdorffDistance                        |
 * | Lines          | LineInterpolatePoint, LineSubstring, LineLocatePoint, StartPoint, EndPoint, ClosestPoint, Azimuth, LineMerge |
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

        val geometryTypeRef = BasicTypeReference(
            "geometry",
            Geometry::class.java,
            SqlTypes.GEOMETRY
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
        // Note: ST_DWithin is not natively available in H2GIS. Use ST_Distance(a,b) <= d instead.

        // ============================
        // METRICS
        // ============================
        f.register("ST_Distance", StandardSQLFunction("ST_Distance", StandardBasicTypes.DOUBLE))
        f.register("ST_Length", StandardSQLFunction("ST_Length", StandardBasicTypes.DOUBLE))
        f.register("ST_Area", StandardSQLFunction("ST_Area", StandardBasicTypes.DOUBLE))
        f.register("ST_HausdorffDistance", StandardSQLFunction("ST_HausdorffDistance", StandardBasicTypes.DOUBLE))

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
        // SERIALISATION
        // ============================
        f.register("ST_AsText", StandardSQLFunction("ST_AsText", StandardBasicTypes.STRING))
        f.register("ST_AsGeoJSON", StandardSQLFunction("ST_AsGeoJSON", StandardBasicTypes.STRING))
        f.register("ST_AsBinary", StandardSQLFunction("ST_AsBinary"))
        f.register("ST_IsValid", StandardSQLFunction("ST_IsValid", StandardBasicTypes.BOOLEAN))
    }
}
