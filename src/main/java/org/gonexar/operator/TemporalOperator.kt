package org.gonexar.operator

import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.SpatialDslContext

interface TemporalOperator {

    /**
     * YEAR(expr)
     */
    fun SpatialExpr<Any>.year(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<Int> {

        val expr = dsl.cb.function(
            "YEAR",
            Int::class.java,
            this.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }

    /**
     * MONTH(expr)
     */
    fun SpatialExpr<Any>.month(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<Int> {

        val expr = dsl.cb.function(
            "MONTH",
            Int::class.java,
            this.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }

    /**
     * DAY(expr)
     */
    fun SpatialExpr<Any>.day(
        dsl: SpatialDslContext<*>
    ): SpatialExpr<Int> {

        val expr = dsl.cb.function(
            "DAY",
            Int::class.java,
            this.expr
        )

        val alias = dsl.autoAlias(expr)

        return dsl.register(alias, expr)
    }
}

