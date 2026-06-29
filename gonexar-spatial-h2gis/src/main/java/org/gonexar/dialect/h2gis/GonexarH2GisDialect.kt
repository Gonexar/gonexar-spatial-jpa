package org.gonexar.dialect.h2gis

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.boot.model.TypeContributions
import org.hibernate.dialect.H2Dialect
import org.hibernate.service.ServiceRegistry

/**
 * Hibernate dialect for **H2 + H2GIS** (in-memory spatial database).
 *
 * This dialect is intended primarily for **integration tests**. H2GIS provides a
 * broad subset of the ISO SQL/MM spatial standard without requiring a running
 * PostgreSQL + PostGIS server, making it ideal for fast CI pipelines.
 *
 * ## Configuration
 *
 * In your test `application.properties`:
 *
 * ```properties
 * spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.database-platform=org.gonexar.dialect.h2gis.GonexarH2GisDialect
 * ```
 *
 * Add H2GIS to your test dependencies in `build.gradle.kts`:
 *
 * ```kotlin
 * testImplementation("org.orbisgis:h2gis:2.2.3")
 * ```
 *
 * You also need to enable H2GIS functions in the H2 session. The easiest way is
 * to add this to your Flyway/Liquibase test migration or `@BeforeAll`:
 *
 * ```sql
 * CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR "org.h2gis.functions.factory.H2GISFunctions.load";
 * CALL H2GIS_SPATIAL();
 * ```
 *
 * ## Limitations vs PostGIS
 *
 * - No `geography` type — metric operations use planar coordinates only.
 * - No raster support — [org.gonexar.operator.MatrixOperator] operations throw
 *   [org.gonexar.dialect.UnsupportedSpatialOperationException] at runtime.
 * - `ST_DWithin` is unavailable; use `ST_Distance(a, b) <= d` as an expression predicate.
 *
 * @see H2GisFunctionContributor
 * @see H2GisTypeContributor
 * @see org.gonexar.dialect.DialectCapabilities.H2GIS
 */
class GonexarH2GisDialect : H2Dialect() {

    override fun contributeTypes(
        typeContributions: TypeContributions,
        serviceRegistry: ServiceRegistry,
    ) {
        super.contributeTypes(typeContributions, serviceRegistry)
        H2GisTypeContributor.registerTypes(typeContributions, serviceRegistry)
    }

    override fun initializeFunctionRegistry(functionContributions: FunctionContributions) {
        super.initializeFunctionRegistry(functionContributions)
        H2GisFunctionContributor.registerFunctions(functionContributions)
    }
}
