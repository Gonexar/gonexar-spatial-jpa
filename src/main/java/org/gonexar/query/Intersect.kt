package org.gonexar.query

import org.gonexar.repository.GonexarSpatialRepository
import org.gonexar.spatial.query
import org.locationtech.jts.geom.Geometry

fun <T : Any> GonexarSpatialRepository<T>.intersects(geom: Geometry): List<T> =
    query(ctx) {
        val geomUser = toGeometryExpr(geom)
        val polygon = geomColumn("polygon")

        where { entity, cb ->
            geomUser.intersects(polygon).toPredicate()
        }
    }
