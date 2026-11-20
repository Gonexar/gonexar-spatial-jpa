package com.gonexar.query

import com.gonexar.repository.GonexarSpatialRepository
import com.gonexar.spatial.query
import org.locationtech.jts.geom.Geometry

inline fun <reified T : Any> GonexarSpatialRepository<T>.intersects(
    geom: Geometry
): List<T> = query<T, T>(ctx) {

    val geomUser = toGeometryExpr(geom)
    val polygon = geomColumn("polygon")

    where { entity, cb ->
        geomUser.intersects(polygon).toPredicate()
    }

    select<T> {
        entity()
    }
}
