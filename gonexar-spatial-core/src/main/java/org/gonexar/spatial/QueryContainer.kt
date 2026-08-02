package org.gonexar.spatial

inline fun <E : Any, reified R : Any> query(
    ctx: SpatialContext<E>,
    noinline block: SpatialDslContainer<E, R>.() -> Unit
): List<R> = query(ctx, R::class.java, block)

fun <E : Any, R : Any> query(
    ctx: SpatialContext<E>,
    resultClass: Class<R>,
    block: SpatialDslContainer<E, R>.() -> Unit
): List<R> {

    val entityManager = ctx.entityManager
    val cb = entityManager.criteriaBuilder

    val query = cb.createQuery(resultClass)
    val root = query.from(ctx.entityClass)

    val criteriaCtx =
        CriteriaDslContext<E, R>(
            cb,
            root
        )

    val dsl =
        SpatialDslContainer(criteriaCtx)

    dsl.block()

    query.select(
        criteriaCtx.projectionOrRoot()
    )

    val predicates =
        criteriaCtx.predicates()

    if (predicates.isNotEmpty()) {
        query.where(*predicates)
    }

    return entityManager
        .createQuery(query)
        .resultList
}


