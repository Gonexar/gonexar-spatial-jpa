package org.gonexar.spatial

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Selection
import org.gonexar.type.Raster
import org.locationtech.jts.geom.Geometry

/**
 * High-level DSL context where spatial operations are written.
 *
 * This is the main interface exposed to developers. All DSL features
 * — such as buffer(), clip(), slope(), summaryStats() — operate on
 * SpatialDslContext.
 *
 * Responsibilities:
 *  - Provide access to CriteriaBuilder and Root
 *  - Register expressions inside CriteriaContext
 *  - Expose conveniences such as raster("rast") and inputGeom()
 *  - Allow definition of the final projection (resultProjection)
 */
class SpatialDslContext<R : Any>(
    val ctx: CriteriaContext<R>,
    val spatial: SpatialContext
) {
    /** Shortcut to CriteriaBuilder from context. */
    val cb: CriteriaBuilder get() = ctx.cb

    /** Shortcut to JPA root entity. */
    val root: Root<*> get() = ctx.root

    /**
     * Registers an expression in the context under a name/alias.
     * Returns the corresponding SpatialExpr.
     */
    fun <T> register(name: String, expr: Expression<T>): SpatialExpr<T> {
        ctx.putExpression(name, expr)
        return SpatialExpr(name, expr)
    }

    /**
     * Retrieves a previously registered expression as a SpatialExpr.
     */
    fun <T> expr(name: String): SpatialExpr<T> =
        SpatialExpr(name, ctx.getExpression(name))

    /**
     * Returns the reference geometry as a SpatialExpr,
     * registering it only once under the alias "input_geom".
     */
    fun inputGeom(): SpatialExpr<Geometry> {
        val alias = "input_geom"
        if (!ctx.containsExpression(alias)) {
            val literal = cb.literal(spatial.referenceGeometry)
            register(alias, literal)
        }
        return expr(alias)
    }

    /**
     * Returns a raster column (PostGIS raster) from the entity root.
     * The column is registered as an Expression inside the context.
     */
    fun raster(fieldName: String): SpatialExpr<Raster> {
        val alias = "raster_$fieldName"
        if (!ctx.containsExpression(alias)) {
            @Suppress("UNCHECKED_CAST")
            val path = root.get<Any>(fieldName) as Expression<Raster>
            register(alias, path)
        }
        return expr(alias)
    }

    /**
     * Returns a geometry column (PostGIS geometry) from the entity root.
     */
    fun geomColumn(fieldName: String): SpatialExpr<Geometry> {
        val alias = "geom_$fieldName"
        if (!ctx.containsExpression(alias)) {
            @Suppress("UNCHECKED_CAST")
            val path = root.get<Geometry>(fieldName) as Expression<Geometry>
            register(alias, path)
        }
        return expr(alias)
    }

    /**
     * Defines the final SELECT projection of the query.
     * This is typically a CriteriaBuilder.construct(...)
     * mapping expressions to a result DTO.
     */
    fun resultProjection(selection: Selection<R>) {
        ctx.setProjection(selection)
    }
}
