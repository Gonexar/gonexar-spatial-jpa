package com.gonexar.dialect.h2gis

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.locationtech.jts.geom.Geometry
import java.time.LocalDate

@Entity
@Table(name = "area")
class Area(
    @Id
    var id: Long = 0,

    var name: String = "",

    @Column(columnDefinition = "GEOMETRY")
    var polygon: Geometry? = null,

    var surveyedAt: LocalDate? = null,
)

@Entity
@Table(name = "route")
class Route(
    @Id
    var id: Long = 0,

    var name: String = "",

    @Column(columnDefinition = "GEOMETRY")
    var path: Geometry? = null,
)

/** DTOs used by projection tests. */
data class NameAndText(val name: String, val wkt: String)

data class NameAndNumber(val name: String, val value: Double)

data class NameAndGeometry(val name: String, val geometry: Geometry)

data class NameAndFlag(val name: String, val flag: Boolean)

data class NameAndDate(val name: String, val year: Int, val month: Int, val day: Int)
