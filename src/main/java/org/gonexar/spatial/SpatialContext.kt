package org.gonexar.spatial

import jakarta.persistence.EntityManager
import org.locationtech.jts.geom.Geometry

/**
 * Represents the input context of a spatial analysis.
 *
 * This class carries all the external information required
 * to execute a geospatial pipeline, including:
 *
 *  - The database EntityManager used for query execution
 *  - The JPA entity class associated with the tile/layer table
 *  - The reference geometry that defines the spatial area of interest
 *  - Optional radius information, used in distance-based analysis
 *
 * It contains NO logic. It is a pure data holder.
 */
data class SpatialContext(
    val entityManager: EntityManager,
    val entityClass: Class<*>,
    val referenceGeometry: Geometry,
    val radiusMeters: Double = 0.0
)