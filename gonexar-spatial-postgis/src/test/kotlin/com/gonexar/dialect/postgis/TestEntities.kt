package com.gonexar.dialect.postgis

import com.gonexar.type.Raster
import com.gonexar.type.RasterUserType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.locationtech.jts.geom.Geometry

@Entity
@Table(name = "area")
class Area(
    @Id
    var id: Long = 0,

    var name: String = "",

    @Column(columnDefinition = "geometry(Polygon, 4326)")
    var polygon: Geometry? = null,
)

@Entity
@Table(name = "route")
class Route(
    @Id
    var id: Long = 0,

    var name: String = "",

    @Column(columnDefinition = "geometry(LineString, 4326)")
    var path: Geometry? = null,
)

@Entity
@Table(name = "elevation_tile")
class ElevationTile(
    @Id
    var id: Long = 0,

    @Type(RasterUserType::class)
    @Column(columnDefinition = "raster")
    var rast: Raster? = null,
)

data class NameAndNumber(val name: String, val value: Double)

data class NameAndGeometry(val name: String, val geometry: Geometry)

data class NameAndText(val name: String, val text: String)

data class TileValue(val id: Long, val value: Double?)

data class TileStats(
    val id: Long,
    val count: Long,
    val sum: Double?,
    val mean: Double?,
    val stddev: Double?,
    val min: Double?,
    val max: Double?,
)
