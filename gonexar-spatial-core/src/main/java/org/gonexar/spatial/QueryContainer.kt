package org.gonexar.spatial

inline fun <E : Any, reified R : Any> query(
    ctx: SpatialContext<E>,
    noinline block: SpatialDslContainer<E, R>.() -> Unit
): List<R> {

    val entityManager = ctx.entityManager
    val cb = entityManager.criteriaBuilder

    val query = cb.createQuery(R::class.java)
    val root = query.from(ctx.entityClass)  // root = entidade

    val criteriaCtx = CriteriaDslContext<E, R>(cb, root)
    val dsl = SpatialDslContainer(criteriaCtx)

    // Executa blocos DSL (projeção, where, expressões)
    dsl.block()

    // caso o DSL tenha definido um projection customizado (DTO)
    val projection = criteriaCtx.projectionOrRoot()
    query.select(projection)

    val predicates = criteriaCtx.predicates()
    if (predicates.isNotEmpty()) {
        query.where(*predicates)
    }

    return entityManager.createQuery(query).resultList
}


