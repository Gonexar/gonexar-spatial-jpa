package org.gonexar.expression

import jakarta.persistence.criteria.Expression

/**
 * Wrapper for a JPA Expression participating in the spatial DSL pipeline.
 *
 * Every spatial operation (buffer, clip, slope, summaryStats, etc.)
 * returns a SpatialExpr, which:
 *
 *   - Holds the alias/name used in the CriteriaContext
 *   - Holds the underlying Expression<T> created by CriteriaBuilder
 *
 * SpatialExpr represents a typed node inside the DSL chain.
 *
 * Examples:
 *   val area = inputGeom().buffer(50)
 *   val clipped = raster("ndvi").clip(area)
 *   val stats = clipped.summaryStats()
 */
class SpatialExpr<T>(
    val name: String,
    val expr: Expression<T>
) {
    /**
     * Creates a new SpatialExpr with the same underlying expression
     * but using a different alias, maintaining the original type T.
     */
    fun alias(newName: String): SpatialExpr<T> =
        SpatialExpr(newName, expr) // Retorna SpatialExpr<T>, sem cast inseguro.
}