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
        val left = cb.function(
            "geography",
            Any::class.java,
            root.get<Geometry>(attr)
        )
        val right = cb.function(
            "geography",
            Any::class.java,
            cb.literal(geom)
        )

        return cb.isTrue(
            cb.function(
                "ST_DWithin",
                Boolean::class.java,
                left,
                right,
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

        // geography(a.polygon)
        val left = cb.function(
            "geography",
            Any::class.java,
            root.get<Geometry>(attr)
        )

        // geography(:geom)
        val right = cb.function(
            "geography",
            Any::class.java,
            cb.literal(geom)
        )

        return cb.function(
            "ST_Distance",
            Double::class.java,
            left,
            right
        )
    }

    fun <T> intersects(
        cb: CriteriaBuilder,
        root: Root<T>,
        attr: String,
        geom: Geometry
    ): Expression<Boolean> =
        cb.isTrue(
            cb.function(
                "ST_Intersects",
                Boolean::class.java,
                root.get<Geometry>(attr),
                cb.literal(geom)
            )
        )
}
