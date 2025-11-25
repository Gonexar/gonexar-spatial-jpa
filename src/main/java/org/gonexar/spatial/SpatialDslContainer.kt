package org.gonexar.spatial

import jakarta.persistence.criteria.Selection
import org.gonexar.expression.SpatialExpr
import org.gonexar.operator.GeometryOperator
import org.gonexar.operator.MathOperator
import org.gonexar.operator.MatrixOperator
import org.gonexar.operator.SpatialOperator
import org.gonexar.operator.TopologyOperator
import org.locationtech.jts.geom.Geometry

class SpatialDslContainer<R : Any>(
    ctx: CriteriaDslContext<R>
) : SpatialDslContext<R>(ctx),
    GeometryOperator,
    MathOperator,
    MatrixOperator,
    SpatialOperator,
    TopologyOperator {

    @Suppress("UNCHECKED_CAST")
    fun <T> SpatialDslContext<R>.selectRoot(): Selection<T> =
        entity as Selection<T>

    fun SpatialExpr<Geometry>.buffer(
        distance: Double
    ): SpatialExpr<Geometry> = this.stBuffer(dsl, distance)

    fun toGeometryExpr(
        geometry: Geometry
    ): SpatialExpr<Geometry> = this.inputGeom(dsl, geometry)

    fun SpatialExpr<Geometry>.intersects(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Boolean> = this.stIntersects(dsl, other)

    fun SpatialExpr<Geometry>.intersection(
        other: SpatialExpr<Geometry>
    ): SpatialExpr<Geometry> = this.stIntersection(dsl, other)
}