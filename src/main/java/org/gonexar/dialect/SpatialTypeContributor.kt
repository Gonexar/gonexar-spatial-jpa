package org.gonexar.dialect

import org.hibernate.boot.model.TypeContributions
import org.hibernate.service.ServiceRegistry

/**
 * Contract for registering spatial Java and JDBC types in a Hibernate dialect.
 *
 * Each supported database provides its own implementation responsible for registering
 * the geometry and raster Java descriptors (JavaType) and JDBC type descriptors (JdbcType)
 * that Hibernate needs to map spatial columns to/from Java objects.
 *
 * ## Why this interface exists
 *
 * Hibernate's type system is extensible via [TypeContributions], but each spatial database
 * uses a different wire format and JDBC driver type:
 *
 * | Database      | Wire format        | JDBC type descriptor               |
 * |---------------|--------------------|------------------------------------|
 * | PostGIS       | WKB (binary)       | `PGGeometryJdbcType.INSTANCE_WKB_2` |
 * | H2GIS         | WKT (text)         | `GeolatteGeometryType`              |
 * | MySQL Spatial | WKB (with 4-byte SRID prefix) | Custom descriptor          |
 *
 * By isolating this registration behind an interface, each dialect module can declare
 * exactly what it needs without affecting the others.
 *
 * ## Implementing a new database
 *
 * ```kotlin
 * class MySQLSpatialTypeContributor : SpatialTypeContributor {
 *     override fun registerTypes(
 *         typeContributions: TypeContributions,
 *         serviceRegistry: ServiceRegistry
 *     ) {
 *         val cfg = typeContributions.typeConfiguration
 *         cfg.javaTypeRegistry.addDescriptor(JTSGeometryJavaType.GEOMETRY_INSTANCE)
 *         cfg.jdbcTypeRegistry.addDescriptor(MySQLGeometryJdbcType.INSTANCE)
 *     }
 * }
 * ```
 *
 * @see SpatialFunctionContributor
 * @see DialectCapabilities
 */
interface SpatialTypeContributor {

    /**
     * Registers Java type descriptors ([org.hibernate.type.descriptor.java.JavaType]) and
     * JDBC type descriptors ([org.hibernate.type.descriptor.jdbc.JdbcType]) required for
     * the target spatial database.
     *
     * This method is called once during Hibernate bootstrap, before any session is created.
     *
     * @param typeContributions Hibernate's contribution context providing access to the type registries.
     * @param serviceRegistry   The Hibernate service registry, used for dependency lookup during setup.
     */
    fun registerTypes(typeContributions: TypeContributions, serviceRegistry: ServiceRegistry)
}
