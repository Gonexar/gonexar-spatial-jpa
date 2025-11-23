package org.gonexar.function

import org.gonexar.spatial.SpatialQuery
import org.gonexar.operator.buffer
import org.gonexar.operator.stAsText
import org.gonexar.spatial.SpatialContext

class BufferQuery(
    context: SpatialContext
) : SpatialQuery<BufferQuery.BufferResult>(context, BufferResult::class) {
    private var radius: Double = 0.0

    override fun build() {
        val geom = dsl.geomColumn("geom")
        val buff = geom.buffer(dsl, radius)
        val wkt = buff.stAsText(dsl)

        dsl.resultProjection(
            cb.construct(BufferResult::class.java, wkt.expr)
        )
    }

    fun radius(radius: Double) {
        this.radius = radius
    }

    data class BufferResult(
        val geometry: String
    )
}
