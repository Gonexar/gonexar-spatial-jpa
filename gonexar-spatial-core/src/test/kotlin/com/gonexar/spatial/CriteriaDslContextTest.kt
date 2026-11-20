package com.gonexar.spatial

import com.gonexar.expression.NumericExpr
import com.gonexar.expression.SpatialExpr
import io.mockk.mockk
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Selection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CriteriaDslContextTest {

    private val root = mockk<Root<String>>()
    private val ctx = CriteriaDslContext<String, Int>(mockk<CriteriaBuilder>(), root)

    @Test
    fun `stores and retrieves expressions by name`() {
        val expr = mockk<Expression<Double>>()
        ctx.putExpression("area", expr)

        assertThat(ctx.containsExpression("area")).isTrue()
        assertThat(ctx.getExpression<Double>("area")).isSameAs(expr)
        assertThat(ctx.containsExpression("missing")).isFalse()
        assertThatThrownBy { ctx.getExpression<Double>("missing") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("'missing'")
    }

    @Test
    fun `collects predicates in order`() {
        val p1 = mockk<Predicate>()
        val p2 = mockk<Predicate>()
        ctx.addPredicate(p1)
        ctx.addPredicate(p2)

        assertThat(ctx.predicates()).containsExactly(p1, p2)
    }

    @Test
    fun `projection falls back to the root when not defined`() {
        assertThat(ctx.projectionOrRoot()).isSameAs(root)
        assertThatThrownBy { ctx.projection() }.isInstanceOf(IllegalStateException::class.java)

        val selection = mockk<Selection<Int>>()
        ctx.setProjection(selection)
        assertThat(ctx.projection()).isSameAs(selection)
        assertThat(ctx.projectionOrRoot()).isSameAs(selection)
    }

    @Test
    fun `aliases keep the same underlying expression`() {
        val expr = mockk<Expression<Double>>()

        val spatial = SpatialExpr("a", expr).alias("b")
        assertThat(spatial.name).isEqualTo("b")
        assertThat(spatial.expr).isSameAs(expr)

        val numeric = NumericExpr<Double>("a", expr).alias("b")
        assertThat(numeric.name).isEqualTo("b")
        assertThat(numeric.expr).isSameAs(expr)
    }
}
