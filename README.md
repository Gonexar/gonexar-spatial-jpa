# Gonexar Spatial
*A DSL espacial para Hibernate + PostGIS, sem native queries.*

O **Gonexar Spatial** é uma biblioteca Java/Kotlin que estende a Criteria API do
Hibernate/JPA para permitir uso de funções espaciais (Geometry e Raster) do
**PostGIS** de forma segura, tipada e mantendo todos os benefícios do ORM.

Ele resolve a maior limitação do Hibernate:
> "Hibernate não suporta funções PostGIS na Criteria API."

Com essa lib, consultas espaciais podem ser escritas como Criteria Queries,
**sem perder relacionamentos, projeções, validação de tipos e lazy loading.**

---

# 🚨 Problema que a lib resolve

Usar PostGIS no JPA normalmente obriga desenvolvedores a usar
**native queries**, causando:

- Perda de relacionamentos (Join, ManyToOne, Lazy…)
- Perda de projections com `CriteriaBuilder.construct(...)`
- Perda de segurança com filtros automáticos
- SQL hardcoded → difícil de manter
- Tipos inconsistentes
- Zero composição ou reuso de expressões espaciais

**O Gonexar Spatial elimina completamente native queries para operações espaciais.**

---

# ✨ Features principais

### ✔ DSL expressiva para funções PostGIS
```kotlin
geom.buffer(this, 50.0)
geom.intersects(this, inputGeom())
route.lineMerge(this).closestPoint(this, input)
´´´

---

# ✔ Suporte completo a Geometry e Raster
ST_Buffer

ST_Intersects

ST_Intersection

ST_ClosestPoint

ST_Distance

ST_Azimuth

ST_LineMerge

ST_LineSubstring

ST_Slope

ST_Clip

ST_SummaryStats

✔ Expression Registry (core da DSL)
Permite compor funções avançadas sem perder referência das expressões.

✔ Dialeto PostgreSQL customizado
Registra automaticamente todas as funções SQL necessárias no Hibernate.

✔ Usável em qualquer microserviço ou aplicação Java
📦 Instalação
kotlin
Copiar código
dependencies {
    implementation("org.gonexar:gonexar-spatial:1.0.0")
}
⚙ Configuração — Hibernate Dialect
application.yml:

yaml
Copiar código
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.gonexar.spatial.GonexarPostgisDialect
🚀 Integração com Spring Boot
kotlin
Copiar código
@Service
class AnalysisService(
    @PersistenceContext val em: EntityManager
) {
    fun analyse(input: Geometry): List<ResultDto> {

        val ctx = SpatialContext(
            entityManager = em,
            entityClass = FeatureEntity::class.java,
            referenceGeometry = input
        )

        return spatial(ctx, ResultDto::class) {

            val geom = geomColumn("geom")
            val buf = geom.buffer(this, 50.0)

            filterIntersects("geom", buf)

            resultProjection(
                cb.construct(ResultDto::class.java, geom.expr, buf.expr)
            )
        }
    }
}
📘 Exemplos completos
🔷 Geometria — Buffer + Intersects
kotlin
Copiar código
val geom = geomColumn("geom")
val buffer = geom.buffer(this, 100.0)
filterIntersects("geom", buffer)
🔷 Geometria — LineMerge + ClosestPoint + Bearing
kotlin
Copiar código
val route = geomColumn("route")
val point = inputGeom()

val merged = route.lineMerge(this)
val closest = merged.closestPoint(this, point)
val bearing = bearingAlongRoute(this, merged, closest)
🔷 Geometria — Percentual dentro de uma área
kotlin
Copiar código
val inter = geom.intersectionWith(this, inputGeom())
val lenInter = inter.lengthGeography(this)
val lenTotal = geom.lengthGeography(this)
val percent = percentageInside(this, lenInter, lenTotal)
🔷 Raster — Slope
kotlin
Copiar código
val rast = raster("rast")
val slope = rast.slope(this)
🔷 Raster — Clip
kotlin
Copiar código
val rast = raster("rast")
val clipped = rast.clip(this, inputGeom())
🔷 Raster — SummaryStats
kotlin
Copiar código
val stats = raster("rast").summaryStats(this)
🧬 Arquitetura interna do DSL
scss
Copiar código
SpatialContext
   ├─ entityManager
   ├─ entityClass
   └─ referenceGeometry

SpatialDslContext
   ├─ cb (CriteriaBuilder)
   ├─ root (Root<*>)
   ├─ register(expr)
   ├─ expr(name)
   ├─ geomColumn()
   ├─ raster()
   └─ inputGeom()

CriteriaContext
   ├─ predicates
   ├─ projection
   └─ registry de expressions   ← 🔥 coração da DSL

SpatialExpr<T>
   ├─ name
   └─ Expression<T>
Fluxo geral
sql
Copiar código
DSL → Expression Registry → Criteria API → SQL gerado pelo Hibernate
🧠 Como eliminamos native queries
Sem a lib:

sql
Copiar código
SELECT ST_Buffer(geom, 50) FROM features
Com a lib:

kotlin
Copiar código
geom.buffer(this, 50.0)
Como funciona?
O Expression Registry:

Guarda cada expressão gerada (ex.: buffer, intersection, slope)

Permite referenciar expressões por nome

Permite compor pipelines (buffer → intersect → length → percent)

Fornece a expressão final ao CriteriaBuilder

Hibernate gera a query SQL válida com funções PostGIS

Isso mantém todo poder do ORM, sem SQL nativo.

🧭 Roadmap
ST_Union

ST_Dump / ST_DumpPoints

ST_SnapToGrid

Suporte a geometrias 3D

Renderizadores / ST_AsMVT

Extensão para análises compostas multi-função