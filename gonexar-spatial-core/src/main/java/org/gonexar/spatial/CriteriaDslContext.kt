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
class CriteriaDslContext<E : Any, R : Any>(
    val cb: CriteriaBuilder,
    val root: Root<E>
) {

    private val _expressions = mutableMapOf<String, Expression<*>>()
    private val _predicates = mutableListOf<Predicate>()
    private var _projection: Selection<R>? = null

    fun putExpression(name: String, expr: Expression<*>) {
        _expressions[name] = expr
    }

    fun containsExpression(name: String): Boolean =
        _expressions.containsKey(name)

    @Suppress("UNCHECKED_CAST")
    fun <T> getExpression(name: String): Expression<T> =
        _expressions[name] as? Expression<T>
            ?: throw IllegalStateException("Expression '$name' not found")

    fun addPredicate(pred: Predicate) {
        _predicates.add(pred)
    }

    fun predicates(): Array<Predicate> =
        _predicates.toTypedArray()

    fun setProjection(sel: Selection<R>) {
        _projection = sel
    }

    @Suppress("UNCHECKED_CAST")
    fun projectionOrRoot(): Selection<R> =
        _projection ?: root as Selection<R> // seguro agora

    fun projection(): Selection<R> =
        _projection ?: throw IllegalStateException("Projection not defined")
}