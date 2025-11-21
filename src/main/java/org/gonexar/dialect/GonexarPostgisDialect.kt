package org.gonexar.dialect

import org.gonexar.type.Raster
import org.gonexar.type.RasterJavaType
import org.gonexar.type.RasterUserType
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
        // --- 2. REGISTRO DO TIPO RASTER CUSTOMIZADO (CRUCIAL) ---

        // A. Adiciona o JavaTypeDescriptor (Opcional, mas boa prática)
        javaRegistry.addDescriptor(RasterJavaType)
        typeContributions.contributeType(RasterUserType, "raster")

        // B. Adiciona o BasicType (Obrigatório para ser mapeado)
        // O BasicType contém a lógica de mapeamento para o SQL (o Extractor/Binder).
        val rasterTypeReference: BasicTypeReference<Raster> = BasicTypeReference(
            "raster", // O nome registrado (RasterType.getName())
            Raster::class.java, // A classe Java/Kotlin
            SqlTypes.VARBINARY // O tipo JDBC subjacente
        )
    }

    override fun initializeFunctionRegistry(functionContributions: FunctionContributions) {
        super.initializeFunctionRegistry(functionContributions)
        val f = functionContributions.functionRegistry

        // IMPORTANT: obtenha a referência ao BasicType a partir do basicTypeRegistry
        // Resolve um BasicTypeReference<Geometry> baseado no Java class Geometry
        // RESOLVE O TIPO GEOMETRY
        //val geometryTypeRef = basic.getRegisteredType<GeometryBasicType>("geometry")
        val basicTypeReference = BasicTypeReference(
            "geometry",
            Geometry::class.java,
            SqlTypes.GEOMETRY
        )

        val rasterTypeReference: BasicTypeReference<Raster> = BasicTypeReference(
            "raster",
            // 1. Sua classe Java/Kotlin: O objeto que o Hibernate manipula
            Raster::class.java,
            // 2. O código JDBC: VARBINARY é o mais apropriado para dados binários complexos (BLOB/RASTER)
            SqlTypes.VARBINARY // Ou java.sql.Types.VARBINARY
        )

        // ============================
        // GEOMETRIA BÁSICA
        // ============================
        f.register("ST_GeometryType", StandardSQLFunction("ST_GeometryType", basicTypeReference))
        f.register("ST_Dimension", StandardSQLFunction("ST_Dimension", basicTypeReference))
        f.register("ST_Envelope", StandardSQLFunction("ST_Envelope", basicTypeReference))
        f.register("ST_Boundary", StandardSQLFunction("ST_Boundary", basicTypeReference))
        f.register("ST_Buffer", StandardSQLFunction("ST_Buffer", basicTypeReference))
        f.register("ST_Simplify", StandardSQLFunction("ST_Simplify", basicTypeReference))
        f.register("ST_Transform", StandardSQLFunction("ST_Transform", basicTypeReference))
        f.register("ST_SetSRID", StandardSQLFunction("ST_SetSRID", basicTypeReference))

        // ============================
        // RELACIONAIS (TOPOLOGIA)
        // ============================
        f.register("ST_Intersects", StandardSQLFunction("ST_Intersects", StandardBasicTypes.BOOLEAN))
        f.register("ST_Contains", StandardSQLFunction("ST_Contains", StandardBasicTypes.BOOLEAN))
        f.register("ST_Within", StandardSQLFunction("ST_Within", StandardBasicTypes.BOOLEAN))
        f.register("ST_Covers", StandardSQLFunction("ST_Covers", StandardBasicTypes.BOOLEAN))
        f.register("ST_CoveredBy", StandardSQLFunction("ST_CoveredBy", StandardBasicTypes.BOOLEAN))
        f.register("ST_Touches", StandardSQLFunction("ST_Touches", StandardBasicTypes.BOOLEAN))
        f.register("ST_Crosses", StandardSQLFunction("ST_Crosses", StandardBasicTypes.BOOLEAN))
        f.register("ST_Overlaps", StandardSQLFunction("ST_Overlaps", StandardBasicTypes.BOOLEAN))
        f.register("ST_DWithin", StandardSQLFunction("ST_DWithin", StandardBasicTypes.BOOLEAN))

        // ============================
        // MÉTRICAS
        // ============================
        f.register("ST_Distance", StandardSQLFunction("ST_Distance", StandardBasicTypes.DOUBLE))
        f.register("ST_Length", StandardSQLFunction("ST_Length", StandardBasicTypes.DOUBLE))
        f.register("geography", StandardSQLFunction("geography")) // cast
        f.register("ST_Area", StandardSQLFunction("ST_Area", StandardBasicTypes.DOUBLE))
        f.register("ST_MaxDistance", StandardSQLFunction("ST_MaxDistance", StandardBasicTypes.DOUBLE))
        f.register("ST_HausdorffDistance", StandardSQLFunction("ST_HausdorffDistance", StandardBasicTypes.DOUBLE))

        // ============================
        // LINHAS / ROTAS
        // ============================
        f.register("ST_LineInterpolatePoint", StandardSQLFunction("ST_LineInterpolatePoint", basicTypeReference))
        f.register("ST_LineSubstring", StandardSQLFunction("ST_LineSubstring", basicTypeReference))
        f.register("ST_LineLocatePoint", StandardSQLFunction("ST_LineLocatePoint", StandardBasicTypes.DOUBLE))
        f.register("ST_StartPoint", StandardSQLFunction("ST_StartPoint", basicTypeReference))
        f.register("ST_EndPoint", StandardSQLFunction("ST_EndPoint", basicTypeReference))
        f.register("ST_ClosestPoint", StandardSQLFunction("ST_ClosestPoint", basicTypeReference))
        f.register("ST_Azimuth", StandardSQLFunction("ST_Azimuth", StandardBasicTypes.DOUBLE))
        f.register("ST_LineMerge", StandardSQLFunction("ST_LineMerge", basicTypeReference))

        // ============================
        // COMBINAÇÃO DE GEOMETRIAS
        // ============================
        f.register("ST_Intersection", StandardSQLFunction("ST_Intersection", basicTypeReference))
        f.register("ST_Union", StandardSQLFunction("ST_Union", basicTypeReference))
        f.register("ST_Difference", StandardSQLFunction("ST_Difference", basicTypeReference))
        f.register("ST_SymDifference", StandardSQLFunction("ST_SymDifference", basicTypeReference))
        f.register("ST_Collect", StandardSQLFunction("ST_Collect", basicTypeReference))

        // ============================
        // RASTER (super importante)
        // ============================
        f.register("ST_Value", StandardSQLFunction("ST_Value", StandardBasicTypes.DOUBLE))
        f.register("ST_NearestValue", StandardSQLFunction("ST_NearestValue", StandardBasicTypes.DOUBLE))
        f.register("ST_Clip", StandardSQLFunction("ST_Clip", rasterTypeReference))
        f.register("ST_SummaryStats", StandardSQLFunction("ST_SummaryStats", StandardBasicTypes.STRING)) // retorna record
        f.register("ST_SummaryStatsAgg", StandardSQLFunction("ST_SummaryStatsAgg", StandardBasicTypes.STRING))
        f.register("ST_MapAlgebraExpr", StandardSQLFunction("ST_MapAlgebraExpr", rasterTypeReference))
        f.register("ST_Reclass", StandardSQLFunction("ST_Reclass", rasterTypeReference))
        f.register("ST_Normalize", StandardSQLFunction("ST_Normalize", rasterTypeReference))
        f.register("ST_Slope", StandardSQLFunction("ST_Slope", rasterTypeReference))
        f.register("ST_Aspect", StandardSQLFunction("ST_Aspect", rasterTypeReference))

        // ============================
        // OUTROS
        // ============================
        f.register("ST_AsText", StandardSQLFunction("ST_AsText", StandardBasicTypes.STRING))
        f.register("ST_AsGeoJSON", StandardSQLFunction("ST_AsGeoJSON", StandardBasicTypes.STRING))
        f.register("ST_AsBinary", StandardSQLFunction("ST_AsBinary"))
        f.register("ST_IsValid", StandardSQLFunction("ST_IsValid", StandardBasicTypes.BOOLEAN))
    }

}
