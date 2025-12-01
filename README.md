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

### 1. Estrutura base da Query
```kotlin
query<Entity, ResultDTO>(ctx) {
// use DSL here
}
````
Componentes principais:
```
geomColumn("field") → pega geometria da entidade
toGeometryExpr(geometry) → converte Geometry → SpatialExpr
where { } → adiciona predicados
select<T> { dto<T> { ... } } → retorna DTO
resultProjection(root) → retorna entidade
```
### 2. Operadores Geométricos (Vector Operators)
#### 2.1 Interseção
```kotlin
val hit = geom1.intersects(geom2)
hit.toPredicate()
````
#### 2.2 Buffer
```kotlin
val buff = geom.buffer(50.0)
````

#### 2.3 Distância
```kotlin
val dist = geom1.distance(geom2)
````
#### 2.4 Interseção geométrica
```kotlin
val inter = geom1.intersection(geom2)
````
#### 2.5 Limpar linhas
```kotlin
val merged = route.lineMerge()
````
#### 2.6 Ponto mais próximo
```kotlin
val pt = geom1.closestPoint(geom2)
````
#### 2.7 Comprimento
```kotlin
val pt = geom1.closestPoint(geom2)
````
#### 2.8 Área
```kotlin
val area = geom.area()
````
#### 2.9 WKT
```kotlin
val wkt = geom.asText()
````

### 3. Operadores Raster
#### 3.1 Acessar raster
```kotlin
val dem = raster("dem")
````
#### 3.2 Recorte raster
```kotlin
val clipped = dem.clip(geom)
````
#### 3.3 Valor raster
```kotlin
val value = stValue(dem, geom)
````

#### 3.4 Slope
```kotlin
val slope = dem.slope()
````
#### 4. WHERE / Predicates
#### 4.1 Usando SpatialExpr
```kotlin
where { _, _ ->
    geom.intersects(area).toPredicate()
}
````
#### 4.2 Usando igualdade comum
```kotlin
where { root, cb ->
    root.get<Int>("id") eq 10
}
````
#### 4.3 Predicados numéricos
```kotlin
root.get<Int>("value") gt 50
root.get<Double>("size") lte 10.5
````
### 5. Literais (para cálculos)
```kotlin
literal(10)       // Int
literal(3.14)     // Double
````
### 6. SELECT – modos de retorno
#### 6.1 Retornando entidades
```kotlin
resultProjection(root)
````

#### 7.2 Retornando DTO
```kotlin
select<MyDTO> {
    dto<MyDTO> {
        field(distance.alias("distance"))
        field(area.alias("area"))
        field(wkt.alias("geometry"))
    }
}
````

### 8. Acesso a colunas da entidade
#### 8.1 Geometria
```kotlin
val poly = geomColumn("polygon")
```

### 8.2 Raster
```kotlin
val dem = raster("dem")
```

### 9. Conversão de geometria literal
```kotlin
val g = toGeometryExpr(userGeometry)
```

### 10. Exemplo super curto (interseção)
```kotlin
query<Area, Area>(ctx) {
val input = toGeometryExpr(geom)
val area = geomColumn("polygon")

    where { _, _ -> area.intersects(input).toPredicate() }

    resultProjection(root)
}
```

### 11. Exemplo completo (DTO)
```kotlin
query<Area, LocationDTO>(ctx) {

    val g = toGeometryExpr(geom)
    val poly = geomColumn("polygon")

    val hit = poly.intersects(g)
    val dist = g.distance(poly)
    val areaVal = poly.area()

    where { _, _ -> hit.toPredicate() }

    select<LocationDTO> {
        dto<LocationDTO> {
            field(dist.alias("distance"))
            field(hit.alias("intersected"))
            field(areaVal.alias("area"))
        }
    }
}
```


### 12. Um exemplo completo:
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
Com o Gonexar Spatial DSL, qualquer consulta espacial pode retornar tanto as entidades do banco (modo JPA tradicional) quanto DTOs customizados (modo API/analytics). A mudança entre os dois modos exige apenas trocar uma linha dentro do DSL.
