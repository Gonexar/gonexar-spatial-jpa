package org.gonexar.spatial

fun <T : Any> query(
    ctx: SpatialContext<T>,
    block: SpatialDslContext<T>.() -> Unit
): List<T> {

    val em = ctx.entityManager
    val cb = em.criteriaBuilder
    val entityClass = ctx.entityClass

    val query = cb.createQuery(entityClass)
    val root = query.from(entityClass)

    // CriteriaContext agora é tipado com T
    val criteriaCtx = CriteriaContext<T>(cb, root)
    val dsl = SpatialDslContext(criteriaCtx)

    // executar a DSL
    dsl.block()

    // caso o DSL tenha definido um projection customizado (DTO)
    val projection = criteriaCtx.projection()
    query.select(projection)

    // aplicar predicados espaciais
    val predicates = criteriaCtx.predicates()
    if (predicates.isNotEmpty()) {
        query.where(*predicates)
    }
    return em.createQuery(query).resultList
}

