package com.gonexar.dialect.postgis

import com.gonexar.dialect.SpatialTypeContributor
import com.gonexar.type.RasterJavaType
import com.gonexar.type.RasterUserType
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
 * | `RasterJavaType` (Gonexar)         | —                                      | `raster`          |
 * | `RasterUserType` (Gonexar)         | via `UserType` contract               | `raster`          |
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

        // Gonexar Raster type: PostGIS custom `raster` type. Values travel as hex-encoded WKB
        // bound with Types.OTHER and are decoded back to bytes when read (see RasterUserType).
        javaRegistry.addDescriptor(RasterJavaType)
        typeContributions.contributeType(RasterUserType)
    }
}
