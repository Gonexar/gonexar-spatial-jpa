package org.gonexar.criteria

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.locationtech.jts.geom.Geometry

object GonexarSpatial {

    fun <T> dWithin(
        cb: CriteriaBuilder,
        root: Root<T>,
        attr: String,
        geom: Geometry,
        meters: Double
    ): Predicate {
        return cb.isTrue(
            cb.function(
                "ST_DWithin",
                Boolean::class.java,
                geographyField(root, cb, attr),
                geographyReference(geom, cb),
                cb.literal(meters)
            )
        )
    }

    fun <T> distance(
        cb: CriteriaBuilder,
        root: Root<T>,
        attr: String,
        geom: Geometry
    ): Expression<Double> {
        return cb.function(
            "ST_Distance",
            Double::class.java,
            geographyField(root, cb, attr),
            geographyReference(geom, cb)
        )
    }

    private fun geographyReference(
        geom: Geometry,
        cb: CriteriaBuilder
    ): Expression<Any> {
        return cb.function(
            "geography",
            Any::class.java,
            cb.literal(geom)
        )
    }

    private fun <T> geographyField(
        root: Root<T>,
        cb: CriteriaBuilder,
        attr: String,
    ): Expression<Any> {
        return cb.function(
            "geography",
            Any::class.java,
            root.get<Geometry>(attr)
        )
    }

    fun <T> intersects(
        cb: CriteriaBuilder,
        root: Root<T>,
        attr: String,
        geom: Geometry
    ): Expression<Boolean> =
        cb.function(
            "ST_Intersects",
            Boolean::class.java,
            root.get<Geometry>(attr),
            cb.literal(geom)
        )
}
