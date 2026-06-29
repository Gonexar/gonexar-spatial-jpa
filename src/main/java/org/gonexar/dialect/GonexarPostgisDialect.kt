package org.gonexar.dialect

import org.gonexar.dialect.postgis.PostgisFunctionContributor
import org.gonexar.dialect.postgis.PostgisTypeContributor
import org.hibernate.boot.model.FunctionContributions
import org.hibernate.boot.model.TypeContributions
import org.hibernate.dialect.PostgreSQLDialect
import org.hibernate.service.ServiceRegistry

/**
 * Hibernate dialect for **PostgreSQL + PostGIS**.
 *
 * Extends [PostgreSQLDialect] and wires in the Gonexar spatial layer by delegating
 * type registration and function registration to dedicated contributor objects.
 * This keeps the dialect class thin and the spatial logic independently testable.
 *
 * ## Configuration
 *
 * Set the following property in your `application.properties` / `application.yml`:
 *
 * ```properties
 * spring.jpa.database-platform=org.gonexar.dialect.GonexarPostgisDialect
 * ```
 *
 * Or in `persistence.xml`:
 *
 * ```xml
 * <property name="hibernate.dialect" value="org.gonexar.dialect.GonexarPostgisDialect"/>
 * ```
 *
 * ## Capabilities
 *
 * This dialect activates **all** PostGIS features:
 * - Geography type and metric-accurate operations (distance in metres, geodesic buffer)
 * - Raster operations (ST_Clip, ST_Slope, ST_Value, ST_SummaryStats, …)
 * - Full ISO SQL/MM topology predicate set
 *
 * See [DialectCapabilities.POSTGIS] for the full capability matrix.
 *
 * ## Extension points
 *
 * To add PostGIS functions without modifying this class, implement [SpatialFunctionContributor]
 * with your extra registrations, then subclass [GonexarPostgisDialect] and override
 * [initializeFunctionRegistry] to call `super` first and delegate to your contributor:
 *
 * ```kotlin
 * class MyExtendedPostgisDialect : GonexarPostgisDialect() {
 *     override fun initializeFunctionRegistry(fc: FunctionContributions) {
 *         super.initializeFunctionRegistry(fc)   // registers all standard PostGIS functions
 *         MyExtraFunctionContributor.registerFunctions(fc)
 *     }
 * }
 * ```
 *
 * Note: [PostgisFunctionContributor] is a Kotlin `object` and cannot be subclassed.
 *
 * @see PostgisFunctionContributor
 * @see PostgisTypeContributor
 * @see DialectCapabilities.POSTGIS
 */
class GonexarPostgisDialect : PostgreSQLDialect() {

    override fun contributeTypes(
        typeContributions: TypeContributions,
        serviceRegistry: ServiceRegistry,
    ) {
        super.contributeTypes(typeContributions, serviceRegistry)
        PostgisTypeContributor.registerTypes(typeContributions, serviceRegistry)
    }

    override fun initializeFunctionRegistry(functionContributions: FunctionContributions) {
        super.initializeFunctionRegistry(functionContributions)
        PostgisFunctionContributor.registerFunctions(functionContributions)
    }
}
