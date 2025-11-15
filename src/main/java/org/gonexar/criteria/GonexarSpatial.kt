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
    ): Predicate =
        cb.isTrue(
            cb.function(
                "ST_DWithin",
                Boolean::class.java,
                root.get<Geometry>(attr),
                cb.literal(geom),      // seu UserType converte para WKT
                cb.literal(meters)
            )
        )

    fun <T> distance(
        cb: CriteriaBuilder,
        root: Root<T>,
        attr: String,
        geom: Geometry
    ): Expression<Double> =
        cb.function(
            "ST_Distance",
            Double::class.java,
            root.get<Geometry>(attr),
            cb.literal(geom)
        )

    fun <T> intersects(
        cb: CriteriaBuilder,
        root: Root<T>,
        attr: String,
        geom: Geometry
    ): Predicate =
        cb.isTrue(
            cb.function(
                "ST_Intersects",
                Boolean::class.java,
                root.get<Geometry>(attr),
                cb.literal(geom)
            )
        )
}
