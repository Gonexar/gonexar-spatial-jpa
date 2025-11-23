package org.gonexar.operator

import org.gonexar.expression.SpatialExpr
import org.gonexar.spatial.SpatialDslContext
import org.gonexar.type.Raster
import org.locationtech.jts.geom.Geometry

fun SpatialDslContext<*>.elevationProfile(
    route: SpatialExpr<Geometry>,
    raster: SpatialExpr<Raster>,
    samples: Int = 100
): SpatialExpr<Any> {

    val merged = route.lineMerge(this)

    // 1) Frações 0..1
    val fractions = (0..samples).map { i -> i.toDouble() / samples }

    // 2) Para cada fração → ponto na linha
    val points = fractions.map { frac ->
        merged.interpolatePoint(this, frac)
    }

    // 3) Para cada ponto → ST_Value(raster, ponto)
    val elevs = points.map { pt ->
        this.stValue(raster, pt)
    }

    // 4) Aqui você pode juntar tudo (lista de pares dist + elev)
    // dependendo de como quer retornar
    val resultExpr = combineElevation(points, elevs)

    return resultExpr
}
