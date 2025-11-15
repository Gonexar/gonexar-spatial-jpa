package org.gonexar.dialect

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.dialect.PostgreSQLDialect
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.query.sqm.function.SqmFunctionRegistry
import org.hibernate.type.StandardBasicTypes

class GonexarPostgisDialect : PostgreSQLDialect() {

    override fun initializeFunctionRegistry(functionContributions: FunctionContributions) {
        super.initializeFunctionRegistry(functionContributions)
        val registry: SqmFunctionRegistry = functionContributions.functionRegistry

        registry.register(
            "ST_DWithin",
            StandardSQLFunction("ST_DWithin", StandardBasicTypes.BOOLEAN)
        )

        registry.register(
            "ST_Distance",
            StandardSQLFunction("ST_Distance", StandardBasicTypes.DOUBLE)
        )

        registry.register(
            "ST_Contains",
            StandardSQLFunction("ST_Contains", StandardBasicTypes.BOOLEAN)
        )

        registry.register(
            "ST_Intersects",
            StandardSQLFunction("ST_Intersects", StandardBasicTypes.BOOLEAN)
        )

        registry.register(
            "ST_AsText",
            StandardSQLFunction("ST_AsText", StandardBasicTypes.STRING)
        )

        registry.register(
            "ST_IsValid",
            StandardSQLFunction("ST_IsValid", StandardBasicTypes.BOOLEAN)
        )

        registry.register(
            "ST_IsValidDetail",
            StandardSQLFunction("ST_IsValidDetail", StandardBasicTypes.STRING)
        )
    }
}
