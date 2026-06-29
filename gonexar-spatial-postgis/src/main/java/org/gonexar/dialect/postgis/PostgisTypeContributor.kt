package org.gonexar.dialect.postgis

import org.gonexar.dialect.SpatialTypeContributor
import org.gonexar.type.RasterJavaType
import org.gonexar.type.RasterUserType
import org.hibernate.boot.model.TypeContributions
import org.hibernate.service.ServiceRegistry
import org.hibernate.spatial.JTSGeometryJavaType
import org.hibernate.spatial.dialect.postgis.PGGeometryJdbcType

/**
 * Registers PostGIS-specific Java and JDBC type descriptors into Hibernate's type system.
 *
 * ## What gets registered
 *
 * | Java Type                          | JDBC Descriptor                        | Column type       |
 * |------------------------------------|----------------------------------------|-------------------|
 * | `JTSGeometryJavaType` (JTS)        | `PGGeometryJdbcType.INSTANCE_WKB_2`    | `geometry`        |
 * | `RasterJavaType` (Gonexar)         | —                                      | `raster` (BYTEA)  |
 * | `RasterUserType` (Gonexar)         | via `UserType` contract                | `raster` (BYTEA)  |
 *
 * ## Wire format
 *
 * PostGIS communicates geometries as **Extended Well-Known Binary (EWKB)**, which includes
 * the SRID as a 4-byte prefix. `PGGeometryJdbcType.INSTANCE_WKB_2` handles the
 * serialisation/deserialisation between EWKB bytes and JTS `Geometry` objects.
 *
 * @see GonexarPostgisDialect
 * @see SpatialTypeContributor
 */
object PostgisTypeContributor : SpatialTypeContributor {

    override fun registerTypes(
        typeContributions: TypeContributions,
        serviceRegistry: ServiceRegistry,
    ) {
        val typeConfig = typeContributions.typeConfiguration
        val javaRegistry = typeConfig.javaTypeRegistry
        val jdbcRegistry = typeConfig.jdbcTypeRegistry

        // JTS Geometry ↔ PostGIS EWKB
        javaRegistry.addDescriptor(JTSGeometryJavaType.GEOMETRY_INSTANCE)
        jdbcRegistry.addDescriptor(PGGeometryJdbcType.INSTANCE_WKB_2)

        // Gonexar Raster type (PostGIS raster stored as BYTEA)
        javaRegistry.addDescriptor(RasterJavaType)
        typeContributions.contributeType(RasterUserType)
    }
}
