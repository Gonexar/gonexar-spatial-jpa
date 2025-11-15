package org.gonexar.function

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import org.gonexar.criteria.GonexarSpatial
import org.gonexar.dto.DataResult
import org.locationtech.jts.geom.Geometry

class GonexarSpatialFunction {
    fun <T : Any, R : Any> spatialDetection(
        entityManager: EntityManager,
        entityClass: Class<T>,
        referenceGeom: Geometry,
        radiusMeters: Double,
        resultClass: Class<R>
    ): List<R> {

        // Descobre automaticamente o campo Geometry
        val geomField = findGeometryField(entityClass)

        val cb = entityManager.criteriaBuilder
        val query = cb.createQuery(resultClass)
        val root = query.from(entityClass)

        // ST_Distance(geography(a.geom), geography(:geom))
        val distance: Expression<Double> = GonexarSpatial.distance(
            cb = cb, root = root, attr = geomField, geom = referenceGeom
        )
        // ST_Intersects(a.geom, :geom)
        val intersects: Expression<Boolean> = GonexarSpatial.intersects(
            cb = cb, root = root, attr = geomField, geom = referenceGeom
        )
        //ST_DWithin(a.geom, a.geom, radius)
        val withinRadius: Predicate = GonexarSpatial.dWithin(
            cb = cb, root = root, attr = geomField, geom = referenceGeom, meters = radiusMeters,
        )
        // SELECT DTO(entity, DataResult(distance, intersects))
        query.select(
            cb.construct(
                resultClass,
                root,
                cb.construct(
                    DataResult::class.java,
                    distance,
                    intersects
                )
            )
        )
            .where(withinRadius)
            .orderBy(cb.asc(distance))

        return entityManager.createQuery(query).resultList
    }

    private fun findGeometryField(entityClass: Class<*>): String {
        for (field in entityClass.declaredFields) {
            if (Geometry::class.java.isAssignableFrom(field.type)) {
                return field.name
            }
        }
        throw IllegalArgumentException(
            "No Geometry field found in entity ${entityClass.simpleName}"
        )
    }
}