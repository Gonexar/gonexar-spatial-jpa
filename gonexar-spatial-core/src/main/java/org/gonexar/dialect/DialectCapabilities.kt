package org.gonexar.dialect

/**
 * Declares the spatial capabilities supported by a specific database dialect.
 *
 * [DialectCapabilities] is a value object attached to each [SpatialFunctionContributor].
 * The Gonexar DSL consults it before executing dialect-specific operations, providing
 * a clear [UnsupportedSpatialOperationException] instead of an opaque SQL exception.
 *
 * ## Usage in the DSL
 *
 * The DSL accesses capabilities through the active [SpatialFunctionContributor]:
 *
 * ```kotlin
 * // Inside a DSL operator:
 * fun SpatialExpr<Geometry>.buffer(distance: Double): SpatialExpr<Geometry> {
 *     return if (dsl.capabilities.supportsGeography) {
 *         // PostGIS: cast to geography for metre-accurate buffering
 *         this.stBuffer(dsl, distance)
 *     } else {
 *         // H2GIS, MySQL: plain planar buffer
 *         this.stBufferPlanar(dsl, distance)
 *     }
 * }
 * ```
 *
 * ## Capability matrix
 *
 * | Capability           | PostGIS | H2GIS | MySQL Spatial | Oracle Spatial |
 * |----------------------|:-------:|:-----:|:-------------:|:--------------:|
 * | supportsGeography    |    ✓    |   ✗   |       ✗       |       ✗        |
 * | supportsRaster       |    ✓    |   ✗   |       ✗       |       ✗        |
 * | supportsTransform    |    ✓    |   ✓   |       ✗       |       ✓        |
 * | supportsCollect      |    ✓    |   ✓   |       ✗       |       ✓        |
 * | supportsLineMerge    |    ✓    |   ✓   |       ✗       |       ✗        |
 * | supportsHausdorff    |    ✓    |   ✓   |       ✗       |       ✗        |
 *
 * @property supportsGeography
 *   Whether the database supports the `geography` type and cast. PostGIS-only.
 *   When `false`, metric operations (distance in metres, geodesic buffering) fall back
 *   to planar equivalents.
 *
 * @property supportsRaster
 *   Whether the database supports PostGIS raster types and functions
 *   (`ST_Value`, `ST_Clip`, `ST_Slope`, `ST_SummaryStats`, etc.).
 *   Raster operations throw [UnsupportedSpatialOperationException] when this is `false`.
 *
 * @property supportsTransform
 *   Whether the database supports `ST_Transform(geom, srid)` for coordinate reference
 *   system re-projection. Requires PROJ4 data to be installed server-side.
 *
 * @property supportsCollect
 *   Whether the database supports `ST_Collect` to merge geometry sets.
 *
 * @property supportsLineMerge
 *   Whether the database supports `ST_LineMerge` to merge linestring collections.
 *
 * @property supportsHausdorff
 *   Whether the database supports `ST_HausdorffDistance`.
 */
data class DialectCapabilities(
    val supportsGeography: Boolean,
    val supportsRaster: Boolean,
    val supportsTransform: Boolean = false,
    val supportsCollect: Boolean = false,
    val supportsLineMerge: Boolean = false,
    val supportsHausdorff: Boolean = false,
) {
    companion object {
        /** Full PostGIS capabilities (geography + raster). */
        val POSTGIS = DialectCapabilities(
            supportsGeography = true,
            supportsRaster = true,
            supportsTransform = true,
            supportsCollect = true,
            supportsLineMerge = true,
            supportsHausdorff = true,
        )

        /** H2GIS capabilities (ISO SQL/MM subset, no geography or raster). */
        val H2GIS = DialectCapabilities(
            supportsGeography = false,
            supportsRaster = false,
            supportsTransform = true,
            supportsCollect = true,
            supportsLineMerge = true,
            supportsHausdorff = true,
        )

        /** MySQL Spatial / MariaDB capabilities (basic ISO SQL/MM). */
        val MYSQL_SPATIAL = DialectCapabilities(
            supportsGeography = false,
            supportsRaster = false,
            supportsTransform = false,
            supportsCollect = false,
            supportsLineMerge = false,
            supportsHausdorff = false,
        )
    }
}

/**
 * Thrown when a spatial operation is invoked on a dialect that does not support it.
 *
 * @param operation Human-readable name of the unsupported spatial operation (e.g., `"ST_Slope"`).
 * @param dialectName Name of the dialect that was active (e.g., `"H2GIS"`).
 */
class UnsupportedSpatialOperationException(
    operation: String,
    dialectName: String,
) : UnsupportedOperationException(
    "Spatial operation '$operation' is not supported by the '$dialectName' dialect. " +
        "Check DialectCapabilities before invoking this operation."
)
