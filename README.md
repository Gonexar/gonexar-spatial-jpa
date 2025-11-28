# Gonexar — Spatial DSL for Kotlin + JPA + PostGIS
Versão: **0.1.0-alpha**

O **Gonexar Spatial DSL** é uma biblioteca que traz o poder do **PostGIS** para dentro do
ecossistema **Kotlin + Spring + JPA Criteria API**, permitindo criar análises espaciais complexas
com poucas linhas de código, sem escrever SQL manual.

> Esta é a versão **alpha/preview**. A API pública continua sujeita a mudanças.

---

## ✨ Por que o Gonexar existe?

PostGIS é extremamente poderoso — mas escrever SQL espacial realista significa lidar com
funções complexas (`ST_Buffer`, `ST_Intersection`, `ST_LineLocatePoint`, `ST_ClosestPoint`, etc.),
com geography casts, alias complicados e dezenas de expressões.

Escrever SQL espacial realista significa lidar com:

- funções difíceis do PostGIS
- aliases e subconsultas
- conversões para geography
- código SQL pouco legível e frágil
- mistura de lógica de domínio com SQL

O Gonexar resolve isso com:

- Sintaxe fluente em Kotlin
- Tipagem forte via Criteria API
- Operadores Compostos (buffer → intersect → distance → percent)
- Retorno de **Entity** ou **DTO**
- Integração transparente com Spring + Hibernate

---

## 🚀 Quickstart

### 1. Instale localmente
```bash
./gradlew publishToMavenLocal
ou
./gradlew publish
```
Adicione o dialect no ``application.properties``:
``spring.jpa.database-platform=org.gonexar.dialect.GonexarPostgisDialect``

### 2. O Gonexar transforma isso em um **DSL legível, fluente e seguro**, como:
Crie uma classe repository que extenda a interface `GonexarSpatialRepository<Entity>`.
Implemente o método `SpatialContext<Entity>`, é através dele que o Gonexar DSL obtém o contexto de persistência da aplicação. Depois disso você pode implementar os seus próprios métodos de consulta espacial.

```kotlin
@Component
class IntersectRepository(
    @PersistenceContext
    private var entityManager: EntityManager
) : GonexarSpatialRepository<Area> {

    override val ctx: SpatialContext<Area>
        get() = SpatialContext(
            entityManager = entityManager,
            entityClass = Area::class.java
        )

    fun intersectLocation(
        geometry: Geometry,
        radiusDetection: Double
    ): List<LocationAnalysisResult> {
        return query<Area, LocationAnalysisResult>(ctx) {
            val geomRequest = toGeometryExpr(geometry)
            val area = geomColumn("polygon")
            val bufferGeomRequest = geomRequest.buffer(radiusDetection)

            val isIntersected = bufferGeomRequest.intersects(area)
            val distance = geomRequest.distance(area)
            val areaSt = area.area()
            val perimeter = area.perimeter()

            where { _, _ ->
                isIntersected.toPredicate()
            }
            select<LocationAnalysisResult> {
                dto<LocationAnalysisDTO> {
                    field(distance.alias("distanceMeters"))
                    field(isIntersected.alias("isIntersected"))
                    field(areaSt.alias("area"))
                    field(perimeter.alias("perimeter"))
                }
                entity()
            }
        }
    }
}
```
Desta forma você pode retornar a propria entidade:
```kotlin
fun intersectLocation(
    geometry: Geometry
): List<Entity> {
    //container DSL
    return query<Entity, Entity>(ctx) {

        val geom = toGeometryExpr(geometry)
        val area = geomColumn("polygon")
        val isIntersected = area.intersects(geom)

        // WHERE com operador espacial
        where { _, _ -> isIntersected.toPredicate() }

        // Retornar a própria entity
        resultProjection(root)
    }
}

```
Com o Gonexar Spatial DSL, qualquer consulta espacial pode retornar tanto as entidades do banco (modo JPA tradicional) quanto DTOs customizados (modo API/analytics). A mudança entre os dois modos exige apenas trocar uma linha dentro do DSL.
