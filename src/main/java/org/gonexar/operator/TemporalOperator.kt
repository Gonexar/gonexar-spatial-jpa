package org.gonexar.operator

import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.SpatialDslContext

interface TemporalOperator {

    fun SpatialExpr<Any>.year(dsl: SpatialDslContext<*>): SpatialExpr<Int> =
        dsl.register("${name}_year", dsl.cb.function("YEAR", Int::class.java, expr))

    fun SpatialExpr<Any>.month(dsl: SpatialDslContext<*>): SpatialExpr<Int> =
        dsl.register("${name}_month", dsl.cb.function("MONTH", Int::class.java, expr))

    fun SpatialExpr<Any>.day(dsl: SpatialDslContext<*>): SpatialExpr<Int> =
        dsl.register("${name}_day", dsl.cb.function("DAY", Int::class.java, expr))
}
