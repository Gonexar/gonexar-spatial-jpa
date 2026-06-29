package org.gonexar.spatial

import jakarta.persistence.criteria.*
import org.gonexar.ast.SelectBuilder
import org.gonexar.expression.NumericExpr
import org.gonexar.expression.SpatialExpr
import org.gonexar.type.Raster
import org.locationtech.jts.geom.Geometry

/**
 * High-level DSL context where spatial operations are written.
 * Responsibilities:
 *  - Provide access to CriteriaBuilder and Root
 *  - Register expressions inside CriteriaContext
 *  - Expose conveniences such as raster("rast") and inputGeom()
 *  - Allow definition of the final projection (resultProjection)
 */
abstract class SpatialDslContext<E : Any, R : Any>(
    val ctx: CriteriaDslContext<E, R>
) {
    val dsl = this

    /** Shortcut to CriteriaBuilder from context. */
    val cb: CriteriaBuilder get() = ctx.cb

    /** Shortcut to JPA root entity. */
    val entity: Root<*> get() = ctx.root

    fun Root<R>.toSelection(): Selection<*> = this as Selection<R>

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
     * Returns a raster column (PostGIS raster) from the entity root.
     * The column is registered as an Expression inside the context.
     */
    fun raster(fieldName: String): SpatialExpr<Raster> {
        val alias = "raster_$fieldName"
        if (!ctx.containsExpression(alias)) {
            @Suppress("UNCHECKED_CAST")
            val path = entity.get<Any>(fieldName) as Expression<Raster>
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
            val path = entity.get<Geometry>(fieldName) as Expression<Geometry>
            register(alias, path)
        }
        return expr(alias)
    }

    /** Gera alias automático baseado na identidade da expressão */
    fun <T> autoAlias(expr: Expression<T>): String =
        "e_${System.identityHashCode(expr)}"

    /**
     * Defines the final SELECT projection of the query.
     * This is typically a CriteriaBuilder.construct(...)
     * mapping expressions to a result DTO.
     */
    fun <X> resultProjection(selection: Selection<X>) {
        @Suppress("UNCHECKED_CAST")
        ctx.setProjection(selection as Selection<R>)
    }

    inline fun <reified R : Any> SpatialDslContainer<*, R>.select(
        block: SelectBuilder<R>.() -> Unit
    ) {
        val builder = SelectBuilder<R>(this)
        builder.block()

        val selection = builder.build<R>(cb, entity)

        resultProjection(selection)
    }

    fun where(predicate: Predicate) {
        ctx.addPredicate(predicate)
    }

    fun where(block: (entity: Root<*>, cb: CriteriaBuilder) -> Predicate) {
        val p = block(entity, cb)
        ctx.addPredicate(p)
    }

    fun SpatialExpr<Boolean>.asPredicate(): Predicate =
        cb.isTrue(this.expr)

    /**
     * Adds an equality predicate to the WHERE clause (path = value).
     */
    fun <T> whereEq(path: Path<T>, value: T) {
        val pred = cb.equal(path, value)
        ctx.addPredicate(pred)
    }

    /**
     * Adds an inequality predicate to the WHERE clause (path != value).
     */
    fun <T> whereNeq(path: Path<T>, value: T) {
        val pred = cb.notEqual(path, value)
        ctx.addPredicate(pred)
    }

    /**
     * Adds a "greater than" predicate to WHERE (path > value).
     */
    fun <N : Number> whereGt(path: Path<N>, value: N) {
        val pred = cb.gt(path as Expression<out Number>, value)
        ctx.addPredicate(pred)
    }

    /**
     * Adds a "greater or equal" predicate to WHERE (path >= value).
     */
    fun <N : Number> whereGte(path: Path<N>, value: N) {
        val pred = cb.ge(path as Expression<out Number>, value)
        ctx.addPredicate(pred)
    }

    /**
     * Adds a "less than" predicate to WHERE (path < value).
     */
    fun <N : Number> whereLt(path: Path<N>, value: N) {
        val pred = cb.lt(path as Expression<out Number>, value)
        ctx.addPredicate(pred)
    }

    /**
     * Adds a "less or equal" predicate to WHERE (path <= value).
     */
    fun <N : Number> whereLte(path: Path<N>, value: N) {
        val pred = cb.le(path as Expression<out Number>, value)
        ctx.addPredicate(pred)
    }

    /**
     * Fluent equality operator: `field eq value`.
     */
    infix fun <T> Path<T>.eq(value: T): Predicate =
        cb.equal(this, value)

    /**
     * Fluent inequality operator: `field neq value`.
     */
    infix fun <T> Path<T>.neq(value: T): Predicate =
        cb.notEqual(this, value)

    /**
     * Fluent "greater than" operator: `field gt value`.
     */
    infix fun <N : Number> Path<N>.gt(value: N): Predicate =
        cb.gt(this as Expression<out Number>, value)

    /**
     * Fluent "greater or equal" operator: `field gte value`.
     */
    infix fun <N : Number> Path<N>.gte(value: N): Predicate =
        cb.ge(this as Expression<out Number>, value)

    /**
     * Fluent "less than" operator: `field lt value`.
     */
    infix fun <N : Number> Path<N>.lt(value: N): Predicate =
        cb.lt(this as Expression<out Number>, value)

    /**
     * Fluent "less or equal" operator: `field lte value`.
     */
    infix fun <N : Number> Path<N>.lte(value: N): Predicate =
        cb.le(this as Expression<out Number>, value)

    /**
     * Creates a literal numeric expression (Int) for DSL operations.
     */
    fun literal(value: Int): NumericExpr<Int> =
        NumericExpr("lit_$value", cb.literal(value))

    /**
     * Creates a literal numeric expression (Double) for computations.
     */
    fun literal(value: Double): NumericExpr<Double> =
        NumericExpr("lit_$value", cb.literal(value))

    /**
     * Converts a SpatialExpr<Boolean> into a Predicate for WHERE clauses.
     */
    fun SpatialExpr<Boolean>.toPredicate(): Predicate =
        dsl.cb.isTrue(this.expr)

    /**
     * Converts a SpatialExpr<T> into a Selection<T> for SELECT/DTO mapping.
     */
    fun <T> SpatialExpr<T>.asSelection(): Selection<T> =
        expr.alias(name)

}
