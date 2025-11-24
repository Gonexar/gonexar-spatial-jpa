package org.gonexar.spatial

fun <T : Any> queryContainer(
    ctx: SpatialContext<T>,
    block: SpatialDslContainer<T>.() -> Unit
): List<T> {

    val entityManager = ctx.entityManager
    val criteriaBuilder = entityManager.criteriaBuilder
    val entityClass = ctx.entityClass

    val query = criteriaBuilder.createQuery(entityClass)
    val root = query.from(entityClass)

    val criteriaCtx = CriteriaDslContext<T>(criteriaBuilder, root)
    val dslContainer = SpatialDslContainer(criteriaCtx)

    // executar a DSL
    dslContainer.block()

    // caso o DSL tenha definido um projection customizado (DTO)
    val projection = criteriaCtx.projectionOrRoot()
    query.select(projection)

    // aplicar predicados espaciais
    val predicates = criteriaCtx.predicates()
    if (predicates.isNotEmpty()) {
        query.where(*predicates)
    }
    return entityManager.createQuery(query).resultList
}

