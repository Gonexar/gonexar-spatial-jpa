package org.gonexar.query

import org.gonexar.operator.inputGeom
import org.gonexar.operator.intersects
import org.gonexar.repository.SpatialRepository
import org.gonexar.spatial.query
import org.locationtech.jts.geom.Geometry

fun <T : Any> SpatialRepository<T>.intersects(geom: Geometry): List<T> =
    query(ctx) {
        val geomUser = inputGeom(this, geom)
        val polygon = geomColumn("polygon")

        where { root, cb ->
            geomUser.intersects(
                this, polygon
            ).toPredicate()
        }
        resultProjection(root)
    }
