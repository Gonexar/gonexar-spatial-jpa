package org.gonexar.spatial

import jakarta.persistence.criteria.Predicate
import kotlin.reflect.KClass

abstract class SpatialQuery<R : Any>(
    context: SpatialContext<R>,
    resultClass: KClass<R>
) {

    private val em = context.entityManager
    private val cb_ = em.criteriaBuilder
    private val query = cb_.createQuery(resultClass.java)
    private val root_ = query.from(context.entityClass)
    private val extraPredicates = mutableListOf<Predicate>()

    protected val criteriaCtx = CriteriaContext<R>(cb_, root_)
    protected val dsl = SpatialDslContext(criteriaCtx)

    // Expor cb e root como antes
    protected val cb get() = dsl.cb
    protected val root get() = dsl.root

    protected abstract fun build()

    fun execute(): List<R> {
        build()
        query.select(criteriaCtx.projection())

        val predicates = criteriaCtx.predicates() + extraPredicates
        if (predicates.isNotEmpty()) query.where(*predicates)

        return em.createQuery(query).resultList
    }

    fun stWhere(field: String = "id", value: Any): SpatialQuery<R> {
        dsl.where { root, cb ->
            cb.equal(root.get<Any>(field), value)
        }
        return this
    }
}
