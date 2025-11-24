package org.gonexar.spatial

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Selection

/**
 * Internal execution context for the spatial analysis DSL.
 *
 * The CriteriaContext is responsible for storing all elements
 * required to build the final CriteriaQuery:
 *
 *   - Expressions (intermediate PostGIS/JPA expressions)
 *   - Predicates  (WHERE conditions)
 *   - Final projection (the Selection<R> that forms the SELECT clause)
 *
 * The DSL uses this context to register intermediate results
 * such as: ST_Buffer, ST_Clip, ST_SummaryStats, ST_Slope, etc.
 *
 * This class contains no geospatial logic. It stores data generated
 * by the DSL in a structured and query-ready form.
 */
class CriteriaDslContext<T : Any>(
    val cb: CriteriaBuilder,
    val root: Root<*>
) {
    private val _expressions = mutableMapOf<String, Expression<*>>()
    private val _predicates = mutableListOf<Predicate>()
    private var _projection: Selection<T>? = null

    /**
     * Registers a typed expression in the context.
     * Typically called by DSL helpers such as buffer(), clip(), summaryStats().
     */
    fun putExpression(name: String, expr: Expression<*>) {
        _expressions[name] = expr
    }

    @Suppress("UNCHECKED_CAST")
    fun projectionOrRoot(): Selection<T> =
        _projection ?: root as Selection<T>

    /**
     * Retrieves a previously registered expression.
     * Throws if the name does not exist.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E> getExpression(name: String): Expression<E> =
        _expressions[name] as? Expression<E>
            ?: throw IllegalStateException("Expression '$name' not found")

    /** Checks whether the expression name exists in the context. */
    fun containsExpression(name: String): Boolean = _expressions.containsKey(name)

    /** Adds a predicate (WHERE clause component). */
    fun addPredicate(pred: Predicate) {
        _predicates.add(pred)
    }

    /** Returns all accumulated predicates. */
    fun predicates(): Array<Predicate> = _predicates.toTypedArray()

    /**
     * Defines the final SELECT projection that the query should return.
     * This is defined inside the DSL using resultProjection(...).
     */
    fun setProjection(sel: Selection<T>) {
        _projection = sel
    }

    /**
     * Retrieves the SELECT projection.
     * Throws if projection was never defined.
     */
    fun projection(): Selection<T> =
        _projection ?: throw IllegalStateException("Projection not defined")

}
