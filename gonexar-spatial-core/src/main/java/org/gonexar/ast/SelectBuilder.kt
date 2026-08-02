package org.gonexar.ast

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Selection
import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.SpatialDslContext

class SelectBuilder<T : Any>(
    val dsl: SpatialDslContext<*, *>
) {
    val rootArgs = mutableListOf<SelectNode>()

    fun field(expr: SpatialExpr<*>) {
        rootArgs += ExprNode(expr.expr)
    }

    fun expr(spatial: SpatialExpr<*>) {
        rootArgs += ExprNode(spatial.expr)
    }

    fun expr(expr: Expression<*>) {
        rootArgs += ExprNode(expr)
    }

    fun entity() {
        rootArgs += EntityNode
    }

    /**
     * Creates a nested DTO projection.
     * We must cast 'dsl' because the underlying DSL object is the same,
     * but the compiler sees a type mismatch between SpatialDslContext<R, *> and SpatialDslContext<T, *>.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> dto(
        block: SelectBuilder<T>.() -> Unit
    ) {
        val nested = SelectBuilder<T>(dsl)
        nested.block()
        rootArgs += NestedDtoNode(T::class.java, nested.rootArgs.toMutableList())
    }

    /**
     * Builds the final Selection object for the Criteria API.
     * This function is correctly marked inline and reified.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified R> build(cb: CriteriaBuilder, root: Root<*>): Selection<R> {
        // We use the R type from the outer class parameter implicitly here
        return buildNode(cb, root, NestedDtoNode(R::class.java, rootArgs)) as Selection<R>
    }

    /**
     * Recursively constructs the JPA selection components.
     */
    @Suppress("UNCHECKED_CAST")
    fun buildNode(cb: CriteriaBuilder, root: Root<*>, node: SelectNode): Selection<*> {
        return when (node) {

            is ExprNode -> node.expr as Selection<*>

            is EntityNode -> root as Selection<*>

            is NestedDtoNode -> {
                val args = node.args.map { buildNode(cb, root, it) }
                    .toTypedArray()

                cb.construct(node.dtoClass, *args)
            }
        }
    }
}