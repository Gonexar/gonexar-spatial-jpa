package org.gonexar.dialect.h2gis

import org.gonexar.dialect.SpatialTypeContributor
import org.hibernate.boot.model.TypeContributions
import org.hibernate.service.ServiceRegistry
import org.hibernate.spatial.JTSGeometryJavaType
import org.hibernate.spatial.GeolatteGeometryJavaType

/**
 * Registers H2GIS-compatible Java and JDBC type descriptors into Hibernate's type system.
 *
 * H2GIS communicates geometries as **Well-Known Text (WKT)** via the standard JDBC
 * `VARCHAR` type, unlike PostGIS which uses EWKB binary. Hibernate Spatial ships
 * built-in support for this through [GeolatteGeometryJavaType] and the H2GIS JDBC dialect.
 *
 * ## What gets registered
 *
 * | Java Type                     | JDBC Descriptor               | Column type    |
 * |-------------------------------|-------------------------------|----------------|
 * | `JTSGeometryJavaType` (JTS)   | H2GIS built-in (WKT via H2Dialect) | `GEOMETRY` |
 * | `GeolatteGeometryJavaType`    | H2GIS built-in                | `GEOMETRY`     |
 *
 * ## Note
 *
 * H2GIS integrates with Hibernate Spatial's H2 support out of the box (since
 * `hibernate-spatial` 6.0+). This contributor ensures the JTS types are available
 * in the same registry as the rest of Gonexar's type system, so the DSL can use
 * `org.locationtech.jts.geom.Geometry` uniformly across dialects.
 *
 * @see GonexarH2GisDialect
 * @see SpatialTypeContributor
 */
object H2GisTypeContributor : SpatialTypeContributor {

    override fun registerTypes(
        typeContributions: TypeContributions,
        serviceRegistry: ServiceRegistry,
    ) {
        val typeConfig = typeContributions.typeConfiguration
        val javaRegistry = typeConfig.javaTypeRegistry

        // Register JTS geometry descriptor so the DSL can use Geometry uniformly
        javaRegistry.addDescriptor(JTSGeometryJavaType.GEOMETRY_INSTANCE)
        javaRegistry.addDescriptor(GeolatteGeometryJavaType.GEOMETRY_INSTANCE)
        // JDBC-side handled by Hibernate Spatial's built-in H2GIS support
    }
}
