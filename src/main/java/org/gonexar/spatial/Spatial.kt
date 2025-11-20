package org.gonexar.spatial

import org.gonexar.data.model.SpatialContext
import kotlin.reflect.KClass

fun <R : Any> spatial(
    spatialContext: SpatialContext,
    resultClass: KClass<R>,
    block: SpatialDslContext<R>.() -> Unit
): List<R> {

    val em = spatialContext.entityManager
    val cb = em.criteriaBuilder
    val query = cb.createQuery(resultClass.java)
    val root = query.from(spatialContext.entityClass)

    val criteriaCtx = CriteriaContext<R>(cb, root)
    val dsl = SpatialDslContext(criteriaCtx, spatialContext)

    dsl.block()

    query.select(criteriaCtx.projection())

    val predicates = criteriaCtx.predicates()
    if (predicates.isNotEmpty()) {
        query.where(*predicates)
    }

    return em.createQuery(query).resultList
}
