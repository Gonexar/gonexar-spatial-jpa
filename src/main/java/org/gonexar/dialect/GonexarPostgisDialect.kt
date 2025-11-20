package org.gonexar.dialect

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.boot.model.TypeContributions
import org.hibernate.dialect.PostgreSQLDialect
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.service.ServiceRegistry
import org.hibernate.spatial.JTSGeometryJavaType
import org.hibernate.spatial.dialect.postgis.PGGeometryJdbcType
import org.hibernate.type.BasicTypeReference
import org.hibernate.type.SqlTypes
import org.hibernate.type.StandardBasicTypes
import org.locationtech.jts.geom.Geometry

class GonexarPostgisDialect : PostgreSQLDialect() {

    override fun contributeTypes(
        typeContributions: TypeContributions,
        serviceRegistry: ServiceRegistry
    ) {
        super.contributeTypes(typeContributions, serviceRegistry)

        val typeConfig = typeContributions.typeConfiguration
        val javaRegistry = typeConfig.javaTypeRegistry
        val jdbcRegistry = typeConfig.jdbcTypeRegistry

        // 1) registre JavaType e JdbcType (se ainda não fez)
        javaRegistry.addDescriptor(JTSGeometryJavaType.GEOMETRY_INSTANCE)
        jdbcRegistry.addDescriptor(PGGeometryJdbcType.INSTANCE_WKB_2)
    }

    override fun initializeFunctionRegistry(functionContributions: FunctionContributions) {
        super.initializeFunctionRegistry(functionContributions)
        val registry = functionContributions.functionRegistry

        // IMPORTANT: obtenha a referência ao BasicType a partir do basicTypeRegistry
        // Resolve um BasicTypeReference<Geometry> baseado no Java class Geometry
        // RESOLVE O TIPO GEOMETRY
        //val geometryTypeRef = basic.getRegisteredType<GeometryBasicType>("geometry")
        val basicTypeReference = BasicTypeReference(
            "geometry",
            Geometry::class.java,
            SqlTypes.GEOMETRY
        )

        registry.registerPattern(
            "ST_ClosestPoint",
            "ST_ClosestPoint(?1, ?2)"
        )

        registry.register(
            "GREATEST",
            StandardSQLFunction(
                "GREATEST",
                StandardBasicTypes.DOUBLE
            )
        )
        registry.register(
            "LEAST",
            StandardSQLFunction(
                "LEAST",
                StandardBasicTypes.DOUBLE
            )
        )

        registry.register(
            "ST_Azimuth",
            StandardSQLFunction(
                "ST_Azimuth",
                StandardBasicTypes.DOUBLE
            )
        )

        registry.register(
            "ST_LineSubstring",
            StandardSQLFunction(
                "ST_LineSubstring",
                basicTypeReference
            )
        )

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
