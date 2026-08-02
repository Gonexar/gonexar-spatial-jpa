package org.gonexar.dialect

import org.hibernate.boot.model.FunctionContributions

/**
 * Contract for registering spatial SQL functions in a Hibernate dialect.
 *
 * Each supported database (PostGIS, H2GIS, MySQL Spatial, etc.) provides its own
 * implementation of this interface. The implementation is responsible for registering
 * every SQL function the database supports into Hibernate's [FunctionContributions] registry,
 * making those functions available for use via [org.hibernate.query.criteria.CriteriaBuilder.function].
 *
 * ## Implementing a new database
 *
 * Create a class that implements [SpatialFunctionContributor] and registers the SQL functions
 * supported by your target database. Then wire it into your Hibernate dialect by delegating
 * [org.hibernate.dialect.Dialect.initializeFunctionRegistry] to this contributor.
 *
 * ```kotlin
 * class MySQLSpatialFunctionContributor : SpatialFunctionContributor {
 *     override fun registerFunctions(functionContributions: FunctionContributions) {
 *         val f = functionContributions.functionRegistry
 *         f.register("ST_Intersects", StandardSQLFunction("ST_Intersects", StandardBasicTypes.BOOLEAN))
 *         // ...
 *     }
 *
 *     override val capabilities: DialectCapabilities
 *         get() = DialectCapabilities(supportsGeography = false, supportsRaster = false)
 * }
 * ```
 *
 * @see DialectCapabilities
 * @see SpatialTypeContributor
 */
interface SpatialFunctionContributor {

    /**
     * Registers all spatial SQL functions into the Hibernate function registry.
     *
     * Implementations must call [FunctionContributions.functionRegistry] to register
     * each supported function. Functions registered here become available for use
     * in JPQL/HQL and in the Gonexar DSL via [org.hibernate.query.criteria.CriteriaBuilder.function].
     *
     * @param functionContributions Hibernate's contribution context for the current dialect.
     */
    fun registerFunctions(functionContributions: FunctionContributions)

    /**
     * Declares the capabilities of the underlying spatial database.
     *
     * This information is used by the DSL to guard dialect-specific operations
     * (e.g., geography casts, raster operations) at the point of use, providing
     * a clear runtime error instead of a cryptic SQL exception.
     *
     * @see DialectCapabilities
     */
    val capabilities: DialectCapabilities
}
