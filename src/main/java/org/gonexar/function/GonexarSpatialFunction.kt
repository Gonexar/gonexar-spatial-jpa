package org.gonexar.function

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import org.gonexar.criteria.GonexarSpatial
import org.gonexar.dto.DataResult
import org.gonexar.dto.RouteProximityResult
import org.locationtech.jts.geom.Geometry

object GonexarSpatialFunction {
    fun <T : Any, R : Any> spatialLocationAnalysis(
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

    fun <T : Any, R : Any> spatialRouteAnalysis(
        entityManager: EntityManager,
        entityClass: Class<T>,
        resultClass: Class<R>,
        route: Geometry,
        radiusMeters: Double
    ): List<R> {

        val geomField = findGeometryField(entityClass)
        val cb = entityManager.criteriaBuilder
        val query = cb.createQuery(resultClass)
        val root = query.from(entityClass)

        // ============================================
        // 1) Normalize route (convert MultiLine to LineString)
        // ============================================
        val mergedRouteExpr: Expression<Geometry> = cb.function(
            "ST_LineMerge",
            Geometry::class.java,
            cb.literal(route)
        )

        // geography(entity.geom)
        val leftGeog: Expression<Any> =
            cb.function("geography", Any::class.java, root.get<Geometry>(geomField))

        // geography(route merged)
        val rightGeog: Expression<Any> =
            cb.function("geography", Any::class.java, mergedRouteExpr)

        // ============================================
        // 2) DISTANCE between entity Polygon and Route
        // ============================================
        val distance: Expression<Double> = cb.function(
            "ST_Distance",
            Double::class.java,
            rightGeog,
            leftGeog

        )

        // ============================================
        // 3) INTERSECTS boolean
        // ============================================
        val intersectsExpr: Expression<Boolean> =
            cb.function(
                "ST_Intersects",
                Boolean::class.java,
                root.get<Geometry>(geomField),
                mergedRouteExpr
            )

        // ============================================
        // 4) CLOSET POINT on route
        // ============================================
        val closestPointExpr: Expression<Geometry> =
            cb.function(
                "ST_ClosestPoint",
                Geometry::class.java,
                mergedRouteExpr,
                root.get<Geometry>(geomField)
            )

        val closestPointWktExpr: Expression<String> =
            cb.function(
                "ST_AsText",
                String::class.java,
                closestPointExpr
            )

        // ============================================
        // 5) INTERSECTION geometry
        // ============================================
        val intersectionGeomExpr: Expression<Geometry> =
            cb.function(
                "ST_Intersection",
                Geometry::class.java,
                root.get<Geometry>(geomField),
                mergedRouteExpr
            )

        val intersectionGeomWktExpr: Expression<String> =
            cb.function(
                "ST_AsText",
                String::class.java,
                intersectionGeomExpr
            )

        // ============================================
        // 6) INTERSECTION LENGTH (meters)
        // ============================================
        val intersectionLengthMetersExpr: Expression<Double> =
            cb.function(
                "ST_Length",
                Double::class.java,
                cb.function("geography", Any::class.java, intersectionGeomExpr)
            )

        // ============================================
        // 7) ROUTE TOTAL LENGTH (meters)
        // ============================================
        val routeLengthMetersExpr: Expression<Double> =
            cb.function(
                "ST_Length",
                Double::class.java,
                cb.function("geography", Any::class.java, mergedRouteExpr)
            )

        // ============================================
        // 8) PERCENTAGE inside polygon
        // ============================================
        val percentInside: Expression<Double> =
            cb.prod(
                cb.quot(
                    intersectionLengthMetersExpr,
                    cb.nullif(routeLengthMetersExpr, cb.literal(0.0))
                ),
                cb.literal(100.0)
            ).`as`(Double::class.java)

        // ============================================
        // 9) BEARING calculation
        // ============================================
        val locateFracExpr: Expression<Double> =
            cb.function(
                "ST_LineLocatePoint",
                Double::class.java,
                mergedRouteExpr,
                closestPointExpr
            )

        val delta = 0.0001

        val startFracExpr: Expression<Double> =
            cb.function(
                "GREATEST",
                Double::class.java,
                cb.diff(locateFracExpr, cb.literal(delta)),
                cb.literal(0.0)
            )

        val endFracExpr: Expression<Double> =
            cb.function(
                "LEAST",
                Double::class.java,
                cb.sum(locateFracExpr, cb.literal(delta)),
                cb.literal(1.0)
            )

        val smallSegmentExpr: Expression<Geometry> =
            cb.function(
                "ST_LineSubstring",
                Geometry::class.java,
                mergedRouteExpr,
                startFracExpr,
                endFracExpr
            )

        val bearingExpr: Expression<Double> =
            cb.function(
                "ST_Azimuth",
                Double::class.java,
                cb.function("ST_StartPoint", Geometry::class.java, smallSegmentExpr),
                cb.function("ST_EndPoint", Geometry::class.java, smallSegmentExpr)
            )

        // ============================================
        // 10) DWITHIN filter
        // ============================================
        val withinRadiusPredicate: Predicate = cb.isTrue(
            cb.function(
                "ST_DWithin",
                Boolean::class.java,
                leftGeog,
                rightGeog,
                cb.literal(radiusMeters)
            )
        )

        // ============================================
        // 11) FINAL PROJECTION
        // ============================================
        query.select(
            cb.construct(
                resultClass,
                root,
                cb.construct(
                    RouteProximityResult::class.java,
                    distance,
                    intersectsExpr,
                    closestPointWktExpr,
                    intersectionGeomWktExpr,
                    intersectionLengthMetersExpr,
                    routeLengthMetersExpr,
                    percentInside,
                    bearingExpr
                )
            )
        )
            .where(withinRadiusPredicate)
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