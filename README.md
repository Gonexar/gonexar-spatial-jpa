![GONEXAR_BANNER_LINKEDIN](https://github.com/user-attachments/assets/5c3eaabf-f778-463a-9e87-29bafb284565)

# Gonexar Spatial JPA

**DSL Kotlin type-safe para consultas espaciais com Hibernate ORM e PostGIS.**

> Versão: **0.1.0-alpha** — A API pública continua sujeita a mudanças.

---

## Índice

1. [O que é?](#o-que-é)
2. [Para que serve?](#para-que-serve)
3. [No que se destaca?](#no-que-se-destaca)
4. [Requisitos e compatibilidade](#requisitos-e-compatibilidade)
5. [Como instalar](#como-instalar)
6. [Conceitos fundamentais](#conceitos-fundamentais)
7. [Operadores disponíveis](#operadores-disponíveis)
8. [Exemplos de implementação](#exemplos-de-implementação)
9. [Sistema de dialetos](#sistema-de-dialetos)
10. [Estrutura do projeto](#estrutura-do-projeto)

---

## O que é?

O **Gonexar Spatial JPA** é uma biblioteca Kotlin que fornece uma DSL (Domain-Specific Language) fluente e type-safe para escrever consultas espaciais usando **JPA Criteria API** com **PostGIS** como backend.

Em vez de escrever SQL espacial manual com strings de função e casts explícitos, você compõe operações geoespaciais como chamadas de método normais em Kotlin — e a biblioteca gera automaticamente o SQL correto para o banco.

**Sem Gonexar:**

```kotlin
// SQL manual, sem type-safety, sem composição
val query = entityManager.createNativeQuery("""
    SELECT
        ST_Distance(
            geography(ST_GeomFromText(:wkt, 4326)),
            geography(a.polygon)
        ) AS distance,
        ST_Area(geography(a.polygon)) AS area
    FROM area a
    WHERE ST_Intersects(
        ST_Buffer(geography(ST_GeomFromText(:wkt, 4326)), :radius)::geometry,
        a.polygon
    ) = true
""")
query.setParameter("wkt", geometry.toText())
query.setParameter("radius", 500.0)
```

**Com Gonexar:**

```kotlin
query<Area, AreaResult>(ctx) {
    val input    = toGeometryExpr(geometry)
    val polygon  = geomColumn("polygon")
    val buffered = input.buffer(500.0)

    where { buffered.intersects(polygon).toPredicate() }

    select<AreaResult> {
        dto<AreaResult> {
            field(input.distance(polygon).alias("distance"))
            field(polygon.area().alias("area"))
        }
    }
}
```

---

## Para que serve?

O Gonexar é projetado para aplicações que precisam de **análise geoespacial dentro de um stack Spring Boot / JPA**, especialmente quando as consultas envolvem:

- **Detecção de proximidade** — encontrar entidades dentro de um raio de distância
- **Análise de sobreposição** — verificar interseção, contenção e adjacência entre geometrias
- **Análise de rota** — calcular posição ao longo de uma rota, ponto mais próximo, azimute
- **Métricas de área e perímetro** — calcular área, comprimento e perímetro com precisão geodésica
- **Processamento raster** — extrair valores de DEM/NDVI, calcular slope, fazer recortes por geometria
- **Análise de cobertura** — percentagem de sobreposição entre geometrias

Casos de uso típicos:

| Domínio | Exemplo de uso |
|---|---|
| Mobilidade urbana | Encontrar rotas que passam por uma área de risco, calcular bearing em tempo real |
| Agricultura de precisão | Extrair NDVI médio de talhões por polígono de fazenda, calcular slope para drenagem |
| Monitoramento ambiental | Detectar áreas desmatadas que intersectam zonas de proteção |
| Gestão de infraestrutura | Encontrar ativos dentro de raio de manutenção, calcular comprimento de rede em área |
| Defesa civil | Analisar áreas de risco que cobrem zonas habitadas |

---

## No que se destaca?

### 1. Type-safety end-to-end

Cada operação retorna `SpatialExpr<T>` com o tipo correto. O compilador Kotlin impede que você passe uma geometria onde se espera um número, ou projete um raster em um campo `String`.

```kotlin
val dist: SpatialExpr<Double>   = a.distance(b)    // Double, não Any
val hit:  SpatialExpr<Boolean>  = a.intersects(b)  // Boolean, usado diretamente no WHERE
val geom: SpatialExpr<Geometry> = a.buffer(100.0)  // Geometry, pode compor mais operações
```

### 2. Lazy evaluation — tudo vira SQL

Nenhuma operação geométrica é executada na JVM. Toda a DSL gera árvores de expressão JPA que são compiladas para SQL apenas quando a query executa. Isso significa que os índices espaciais do PostgreSQL são sempre aproveitados.

### 3. Suporte a Raster (único no ecossistema JVM)

O Gonexar é a única biblioteca no ecossistema JVM que suporta operações PostGIS Raster (`ST_Clip`, `ST_Slope`, `ST_Value`, `ST_SummaryStats`) diretamente em consultas JPA, com tipo `Raster` mapeado nativamente.

### 4. Composição natural de operações

Operações podem ser compostas encadeando chamadas — o resultado de um `buffer()` pode ser passado direto para um `intersects()`, que vira predicado no `where()`.

```kotlin
val risk = toGeometryExpr(riskZone)
val asset = geomColumn("location")

// composição: buffer → intersects → predicado → where
where { risk.buffer(200.0).intersects(asset).toPredicate() }
```

### 5. Retorno flexível: entidade ou DTO

A mesma query pode retornar a entidade JPA completa ou um DTO customizado com campos calculados — mudando apenas o bloco `select`.

---

## Requisitos e compatibilidade

### Runtime obrigatório

| Componente | Versão mínima | Notas |
|---|---|---|
| Kotlin | 1.9.x | Usa extension functions e inline reified |
| Java | 17 | Compilado com `toolchain { languageVersion = 17 }` |
| Hibernate ORM | 6.4.x | Usa `TypeContributions` e `FunctionContributions` da API 6 |
| Jakarta Persistence | 3.1 | JPA 3.0 — incompatível com `javax.persistence` |
| PostgreSQL | 12+ | Qualquer versão com PostGIS instalado |
| PostGIS | 3.x recomendado | 2.5+ funciona para a maioria das operações |

### Runtime opcional (para raster)

| Componente | Versão | Notas |
|---|---|---|
| PostGIS Raster | instalado no servidor | Necessário para `ST_Value`, `ST_Clip`, `ST_Slope` |
| postgis-jdbc | 2.5.0 | Driver JDBC para tipos PostGIS |

### Para testes (sem Docker)

| Componente | Versão |
|---|---|
| H2 Database | 2.2.x |
| H2GIS | 2.2.x |

### Compatibilidade com frameworks

| Framework | Status |
|---|---|
| Spring Boot 3.x | ✓ Compatível (usa Jakarta EE) |
| Spring Boot 2.x | ✗ Incompatível (usa `javax.persistence`) |
| Quarkus (Hibernate ORM 6) | ✓ Compatível (configuração manual) |
| Micronaut Data JPA | Não testado |

---

## Como instalar

### 1. Publicar localmente (desenvolvimento)

```bash
./gradlew publishToMavenLocal
```

### 2. Adicionar a dependência

```kotlin
// build.gradle.kts
repositories {
    mavenLocal()
    // ou o repositório Maven do Gonexar quando publicado
}

dependencies {
    implementation("com.gonexar:gonexar-spatial-jpa:0.1.0-alpha")
}
```

### 3. Configurar o dialect

```properties
# application.properties
spring.jpa.database-platform=org.gonexar.dialect.GonexarPostgisDialect
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/meu_banco
spring.datasource.username=usuario
spring.datasource.password=senha
```

### 4. Verificar que PostGIS está ativo no banco

```sql
-- Execute no banco PostgreSQL para confirmar
SELECT PostGIS_Version();
-- Resultado esperado: 3.x.x ...
```

### 5. Mapear colunas geométricas nas entidades

```kotlin
@Entity
@Table(name = "area")
data class Area(
    @Id
    val id: Long = 0,

    val name: String = "",

    // coluna geometry do PostGIS — mapeada pelo Gonexar dialect
    @Column(name = "polygon", columnDefinition = "geometry(Polygon, 4326)")
    val polygon: Geometry? = null,

    // coluna raster do PostGIS (opcional)
    @Column(name = "ndvi", columnDefinition = "raster")
    @Type(RasterUserType::class)
    val ndvi: Raster? = null
)
```

---

## Conceitos fundamentais

### `SpatialContext<E>`

Ponto de entrada que conecta o DSL ao `EntityManager` e à entidade JPA:

```kotlin
val ctx = SpatialContext(entityManager, Area::class.java)
```

### `GonexarSpatialRepository<E>`

Interface que seu repository implementa para ter acesso ao DSL:

```kotlin
@Component
class AreaRepository(
    @PersistenceContext private val em: EntityManager
) : GonexarSpatialRepository<Area> {

    override val ctx: SpatialContext<Area>
        get() = SpatialContext(em, Area::class.java)
}
```

### `query<E, R> { ... }`

Função de extensão que executa uma query DSL e retorna `List<R>`:

```kotlin
val results: List<AreaResult> = query<Area, AreaResult>(ctx) {
    // bloco DSL aqui
}
```

### `SpatialExpr<T>`

Wrapper lazy de `Expression<T>` do JPA. Representa o resultado de qualquer operação espacial — nunca contém o valor em si, apenas a expressão que produzirá o valor no SQL.

```kotlin
val expr: SpatialExpr<Double> = polygon.area()
// Nenhum cálculo acontece aqui — apenas uma expressão SQL é construída
```

### Entry points dentro do bloco `query`

```kotlin
query<Area, Result>(ctx) {
    // Coluna de geometria da entidade
    val polygon = geomColumn("polygon")

    // Coluna de raster da entidade
    val ndvi = raster("ndvi")

    // Geometria fornecida pelo usuário (parâmetro da query)
    val input = toGeometryExpr(myGeometry)

    // Ponto literal
    val point = literalPoint(Coordinate(-43.1, -22.9), srid = 4326)
}
```

---

## Operadores disponíveis

Todos os operadores são extension functions disponíveis dentro do bloco `query { }`.

---

### Operadores de Geometria

Transformações e construções geométricas — retornam `SpatialExpr<Geometry>`.

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `.buffer(distance)` | `ST_Buffer(geography, d)` | Cria um polígono de zona de influência ao redor da geometria. `distance` em metros (via cast geography). Uso típico: raio de detecção, zona de segurança. |
| `.simplify(tolerance)` | `ST_Simplify(geom, t)` | Reduz o número de vértices de uma geometria preservando sua forma. Útil para performance em exibição de mapas. |
| `.transform(srid)` | `ST_Transform(geom, srid)` | Reprojecta a geometria para outro sistema de coordenadas. Ex: de WGS84 (4326) para UTM (31983). |
| `.setSrid(srid)` | `ST_SetSRID(geom, srid)` | Define o SRID sem reprojectar — útil quando a coluna não tem SRID definido. |
| `.intersection(other)` | `ST_Intersection(a, b)` | Retorna a geometria resultante da sobreposição entre duas geometrias. |
| `.union(other)` | `ST_Union(a, b)` | Une duas geometrias em uma só. |
| `.difference(other)` | `ST_Difference(a, b)` | Retorna a parte da geometria `a` que não está em `b`. |
| `.symDifference(other)` | `ST_SymDifference(a, b)` | Retorna as partes que existem em `a` ou em `b`, mas não em ambas. |
| `.stEnvelope(dsl)` | `ST_Envelope(geom)` | Retorna o bounding box (retângulo envolvente) da geometria. |
| `.stBoundary(dsl)` | `ST_Boundary(geom)` | Retorna o contorno (boundary) da geometria. |

**Exemplos:**

```kotlin
// Zona de 500m ao redor de um ponto de interesse
val zona = input.buffer(500.0)

// Área de interseção entre dois polígonos
val sobreposicao = poligono1.intersection(poligono2)

// Simplificar geometria complexa para exibição em zoom alto
val simplificado = poligono.simplify(0.001)

// Reprojectar para UTM para cálculos em metros
val utmGeom = poligono.transform(31983)
```

---

### Operadores Topológicos (Predicados Espaciais)

Relações espaciais — retornam `SpatialExpr<Boolean>`, usados diretamente no `where`.

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `.intersects(other)` | `ST_Intersects(a, b)` | Verdadeiro se as geometrias têm qualquer ponto em comum. O predicado espacial mais comum. |
| `.contains(other)` | `ST_Contains(a, b)` | Verdadeiro se `a` contém completamente `b` e nenhum ponto de `b` está no contorno de `a`. |
| `.within(other)` | `ST_Within(a, b)` | Inverso de `contains` — verdadeiro se `a` está completamente dentro de `b`. |
| `.covers(other)` | `ST_Covers(a, b)` | Verdadeiro se nenhum ponto de `b` está fora de `a`. Mais permissivo que `contains`. |
| `.coveredBy(other)` | `ST_CoveredBy(a, b)` | Inverso de `covers`. |
| `.touches(other)` | `ST_Touches(a, b)` | Verdadeiro se as geometrias se tocam apenas no contorno, sem se sobrepor. |
| `.crosses(other)` | `ST_Crosses(a, b)` | Verdadeiro se as geometrias se cruzam (interseção com dimensão inferior). |
| `.overlaps(other)` | `ST_Overlaps(a, b)` | Verdadeiro se as geometrias têm sobreposição parcial (mesma dimensão). |
| `.dWithin(other, d)` | `ST_DWithin(a, b, d)` | Verdadeiro se a distância entre `a` e `b` é menor ou igual a `d`. |

**Exemplos:**

```kotlin
val input   = toGeometryExpr(geometriaUsuario)
val poligono = geomColumn("area")

// Filtrar áreas que intersectam a geometria de busca
where { input.intersects(poligono).toPredicate() }

// Filtrar pontos dentro de um município
where { ponto.within(municipio).toPredicate() }

// Filtrar ativos dentro de 1km de um ponto
where { ativo.dWithin(input, 1000.0).toPredicate() }

// Combinar predicados
where {
    cb.and(
        input.intersects(poligono).toPredicate(),
        entity.get<String>("status") eq "ATIVO"
    )
}
```

---

### Operadores Métricos

Cálculos de distância, área e comprimento — retornam `SpatialExpr<Double>`.

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `.distance(other)` | `ST_Distance(a, b)` | Distância planar entre duas geometrias (em unidades do CRS). |
| `.distance(other, useGeography=true)` | `ST_Distance(geog(a), geog(b))` | Distância geodésica em **metros**. Correto para WGS84. |
| `.area()` | `ST_Area(geography(geom))` | Área da geometria em **metros quadrados** (via cast geography). |
| `.length()` | `ST_Length(geom)` | Comprimento planar da geometria linear. |
| `.lengthGeography()` | `ST_Length(geography(geom))` | Comprimento geodésico em **metros**. |
| `.perimeter()` | `ST_Perimeter(geom)` | Perímetro planar do polígono. |
| `.perimeter(useGeography=true)` | `ST_Perimeter(geography(geom))` | Perímetro geodésico em **metros**. |
| `.stDistance(dsl, other)` | `ST_Distance(geog(a), geog(b))` | Versão explícita com controle de geography. |

**Exemplos:**

```kotlin
val input   = toGeometryExpr(pontoUsuario)
val poligono = geomColumn("area")

// Distância em metros (geodésica) entre ponto e polígono
val distMetros = input.distance(poligono, useGeography = true)

// Área em m² de cada polígono retornado
val areaMq = poligono.area()

// Comprimento de uma rota em metros
val rota = geomColumn("route")
val comprimento = rota.lengthGeography()

// Percentual dentro de uma zona
val intersecao = rota.intersection(zona)
val pct = percentageInsideIntersection(
    intersecao.lengthGeography(),
    rota.lengthGeography()
)
```

---

### Operadores de Rota e Linha

Operações sobre geometrias lineares (LineString) — essenciais para análise de rotas.

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `.startPoint()` | `ST_StartPoint(geom)` | Retorna o primeiro ponto da linha. |
| `.endPoint()` | `ST_EndPoint(geom)` | Retorna o último ponto da linha. |
| `.closestPoint(other)` | `ST_ClosestPoint(a, b)` | Retorna o ponto de `a` mais próximo de `b`. |
| `.lineLocatePoint(point)` | `ST_LineLocatePoint(line, pt)` | Retorna a fração (0.0–1.0) da linha onde o ponto está mais próximo. |
| `.stLineInterpolatePoint(fraction)` | `ST_LineInterpolatePoint(geom, f)` | Retorna o ponto na fração `f` da linha. |
| `.lineSubstring(start, end)` | `ST_LineSubstring(geom, s, e)` | Retorna o trecho da linha entre as frações `start` e `end`. |
| `.lineMerge()` | `ST_LineMerge(geom)` | Une uma coleção de LineStrings conectadas em linhas simples. |
| `.stDistance(dsl, other)` com `.stClosestPoint` | `ST_Azimuth(a, b)` | Usado internamente pelo `bearingAlongRoute`. |
| `bearingAlongRoute(route, point)` | composição de ST_LineLocatePoint + ST_Azimuth | Calcula o azimute (direção em radianos) da rota no ponto mais próximo. |

**Exemplos:**

```kotlin
val rota = geomColumn("route")
val veiculo = toGeometryExpr(posicaoVeiculo)

// Ponto da rota mais próximo do veículo
val ptProximo = rota.closestPoint(veiculo)

// Fração (0.0 a 1.0) onde o veículo está na rota
val fracao = rota.lineLocatePoint(veiculo)

// Geometria do ponto exato na rota
val ptNaRota = rota.stLineInterpolatePoint(0.5) // meio da rota

// Trecho de 20% a 80% da rota
val trecho = rota.lineSubstring(0.2, 0.8)

// Direção da rota no ponto do veículo (em radianos)
val direcao = bearingAlongRoute(rota, veiculo, alias = "bearing")
```

---

### Operadores de Serialização

Conversão da geometria para formatos de texto ou binário.

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `.asText()` | `ST_AsText(geom)` | Converte para WKT (Well-Known Text). Ex: `"POINT(-43.1 -22.9)"`. |
| `.asBinary()` | `ST_AsBinary(geom)` | Converte para WKB (Well-Known Binary). |
| `.stAsText(dsl)` | `ST_AsText(geom)` | Versão alternativa com passagem explícita de DSL. |
| `.stGeometryType(dsl)` | `ST_GeometryType(geom)` | Retorna o nome do tipo: `"ST_Point"`, `"ST_Polygon"`, etc. |

**Exemplos:**

```kotlin
val poligono = geomColumn("area")

// Retornar WKT na projeção
val wkt = poligono.asText()

// Retornar tipo geométrico
val tipo = poligono.stGeometryType(dsl) // "ST_Polygon"

select<AreaResult> {
    dto<AreaResult> {
        field(wkt.alias("wkt"))
        field(tipo.alias("geometryType"))
    }
}
```

---

### Operadores Raster

Operações sobre dados raster (DEM, NDVI, etc.) — disponíveis apenas com `GonexarPostgisDialect`.

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `raster("campo")` | — | Acessa uma coluna raster da entidade. |
| `.stValue(dsl, geom)` | `ST_Value(rast, geom)` | Extrai o valor do pixel raster na posição de uma geometria ponto. |
| `.stClip(dsl, geom)` | `ST_Clip(rast, geom)` | Recorta o raster pela geometria. Retorna `SpatialExpr<Raster>`. |
| `.slope(dsl)` | `ST_Slope(rast, '32BF')` | Calcula o mapa de declividade a partir de um DEM. |
| `.stSummaryStats(dsl)` | `ST_SummaryStats(rast)` | Retorna estatísticas do raster: min, max, mean, stddev, count, sum. |

**`RasterStatsExpr`** — projeção de estatísticas:

```kotlin
data class RasterStatsExpr(
    val min:    SpatialExpr<Double?>,
    val max:    SpatialExpr<Double?>,
    val mean:   SpatialExpr<Double?>,
    val stddev: SpatialExpr<Double?>,
    val count:  SpatialExpr<Long>,
    val sum:    SpatialExpr<Double?>,
)
```

**Exemplos:**

```kotlin
val talhao  = geomColumn("boundary")
val ndvi    = raster("ndvi")
val dem     = raster("elevation")

// Valor NDVI no centroide do talhão
val ndviPonto = literalPoint(Coordinate(-47.2, -19.8))
val ndviValor = ndvi.stValue(dsl, ndviPonto)

// Recortar NDVI pelo polígono do talhão
val ndviRecortado = ndvi.stClip(dsl, talhao)

// Estatísticas do NDVI na área do talhão
val stats = ndviRecortado.stSummaryStats(dsl)

// Declividade do terreno
val slope = dem.stClip(dsl, talhao).slope(dsl)

// Projetar tudo em um DTO
select<TalhaoAnalysis> {
    dto<TalhaoAnalysis> {
        field(stats.mean.alias("ndviMedio"))
        field(stats.min.alias("ndviMin"))
        field(stats.max.alias("ndviMax"))
        field(ndviValor.alias("ndviPonto"))
    }
}
```

---

### Operadores Temporais

Extração de componentes de data — retornam `SpatialExpr<Int>`.

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `.year()` | `YEAR(expr)` | Extrai o ano de uma expressão de data. |
| `.month()` | `MONTH(expr)` | Extrai o mês (1–12). |
| `.day()` | `DAY(expr)` | Extrai o dia do mês (1–31). |

---

### Operadores Matemáticos

| Operador DSL | Função SQL | Descrição |
|---|---|---|
| `generateSeries(start, stop, step)` | `generate_series(s, e, step)` | Gera uma série numérica. Usado para interpolação de rota com N amostras. |
| `.interpolatePoint(fraction)` | `ST_LineInterpolatePoint(geom, f)` | Versão que aceita `NumericExpr<Double>` para fração dinâmica. |
| `percentageInsideIntersection(intersect, total)` | `(intersect / NULLIF(total, 0)) * 100` | Calcula percentagem de sobreposição entre dois comprimentos. |
| `bearingAlongRoute(route, point)` | composição SQL | Calcula azimute da rota no ponto mais próximo do input. |

---

## Exemplos de implementação

### Exemplo 1 — Análise de proximidade (o mais comum)

Encontrar todas as áreas que intersectam com uma geometria de busca, retornando distância e área calculadas.

```kotlin
// DTO de resultado
data class AreaProximidadeResult(
    val id: Long,
    val name: String,
    val distanceMetros: Double,
    val areaMq: Double,
    val intersecta: Boolean
)

@Component
class AreaRepository(
    @PersistenceContext private val em: EntityManager
) : GonexarSpatialRepository<Area> {

    override val ctx: SpatialContext<Area>
        get() = SpatialContext(em, Area::class.java)

    fun buscarPorProximidade(
        geometria: Geometry,
        raioMetros: Double
    ): List<AreaProximidadeResult> = query<Area, AreaProximidadeResult>(ctx) {

        val input    = toGeometryExpr(geometria)
        val poligono = geomColumn("polygon")

        // Expande a geometria de busca pelo raio desejado
        val zona = input.buffer(raioMetros)

        // Operações — todas lazy, viram SQL
        val intersecta  = zona.intersects(poligono)
        val distancia   = input.distance(poligono, useGeography = true)
        val area        = poligono.area()

        // Filtro espacial
        where { intersecta.toPredicate() }

        select<AreaProximidadeResult> {
            dto<AreaProximidadeResult> {
                field(entity.get<Long>("id").alias("id"))
                field(entity.get<String>("name").alias("name"))
                field(distancia.alias("distanceMetros"))
                field(area.alias("areaMq"))
                field(intersecta.alias("intersecta"))
            }
        }
    }
}
```

SQL gerado equivalente:

```sql
SELECT
    a.id,
    a.name,
    ST_Distance(geography(?), geography(a.polygon)) AS distanceMetros,
    ST_Area(geography(a.polygon))                   AS areaMq,
    ST_Intersects(ST_Buffer(geography(?), ?)::geometry, a.polygon) AS intersecta
FROM area a
WHERE ST_Intersects(ST_Buffer(geography(?), ?)::geometry, a.polygon) = true
```

---

### Exemplo 2 — Análise de rota com veículo

Calcular a posição de um veículo em uma rota, percentagem percorrida, bearing e trecho restante.

```kotlin
data class PosicaoRotaResult(
    val routeId: Long,
    val fracao: Double,
    val distanciaPercorrida: Double,
    val distanciaRestante: Double,
    val bearing: Double,
    val wktPosicaoNaRota: String
)

fun analisarPosicaoNaRota(
    posicaoVeiculo: Geometry,
    routeId: Long
): PosicaoRotaResult? = query<Route, PosicaoRotaResult>(ctx) {

    val veiculo = toGeometryExpr(posicaoVeiculo)
    val rota    = geomColumn("geometry")

    // Ponto da rota mais próximo do veículo
    val ptProximo = rota.closestPoint(veiculo)

    // Fração (0.0–1.0) de onde o veículo está na rota
    val fracao = rota.lineLocatePoint(ptProximo)

    // Comprimento total da rota em metros
    val comprimentoTotal = rota.lengthGeography()

    // Trecho já percorrido e restante
    val percorrido = rota.lineSubstring(literal(0.0), fracao)
    val restante   = rota.lineSubstring(fracao, literal(1.0))

    val distPercorrida = percorrido.lengthGeography()
    val distRestante   = restante.lengthGeography()

    // Direção do veículo (azimute em radianos)
    val bearing = bearingAlongRoute(rota, veiculo, alias = "bearing")

    // WKT do ponto exato na rota
    val wktPosicao = ptProximo.asText()

    where { entity.get<Long>("id") eq routeId }

    select<PosicaoRotaResult> {
        dto<PosicaoRotaResult> {
            field(entity.get<Long>("id").alias("routeId"))
            field(fracao.alias("fracao"))
            field(distPercorrida.alias("distanciaPercorrida"))
            field(distRestante.alias("distanciaRestante"))
            field(bearing.alias("bearing"))
            field(wktPosicao.alias("wktPosicaoNaRota"))
        }
    }
}.firstOrNull()
```

---

### Exemplo 3 — Análise de cobertura de rota por zona de risco

Calcular quanto de uma rota passa por dentro de uma área de risco.

```kotlin
data class CoberturRiscoResult(
    val routeId: Long,
    val comprimentoTotalMetros: Double,
    val comprimentoEmRiscoMetros: Double,
    val percentualEmRisco: Double
)

fun calcularCobertura(
    zonaRisco: Geometry
): List<CoberturRiscoResult> = query<Route, CoberturRiscoResult>(ctx) {

    val zona = toGeometryExpr(zonaRisco)
    val rota  = geomColumn("geometry")

    // Comprimento total da rota
    val comprimentoTotal = rota.lengthGeography()

    // Trecho da rota dentro da zona de risco
    val trechoEmRisco = rota.intersection(zona)
    val comprimentoRisco = trechoEmRisco.lengthGeography()

    // Percentual — protegido contra divisão por zero
    val percentual = percentageInsideIntersection(
        comprimentoRisco,
        comprimentoTotal,
        alias = "percentualEmRisco"
    )

    // Só retornar rotas que passam pela zona
    where { rota.intersects(zona).toPredicate() }

    select<CoberturRiscoResult> {
        dto<CoberturRiscoResult> {
            field(entity.get<Long>("id").alias("routeId"))
            field(comprimentoTotal.alias("comprimentoTotalMetros"))
            field(comprimentoRisco.alias("comprimentoEmRiscoMetros"))
            field(percentual.alias("percentualEmRisco"))
        }
    }
}
```

---

### Exemplo 4 — Análise NDVI de talhão agrícola (Raster)

Extrair estatísticas de NDVI para polígonos de talhões, calculando média, máximo e desvio padrão.

```kotlin
data class NdviTalhaoResult(
    val talhaoId: Long,
    val ndviMedio: Double?,
    val ndviMax: Double?,
    val ndviMin: Double?,
    val ndviStdDev: Double?,
    val areaTalhaoMq: Double
)

@Component
class TalhaoRepository(
    @PersistenceContext private val em: EntityManager
) : GonexarSpatialRepository<Talhao> {

    override val ctx: SpatialContext<Talhao>
        get() = SpatialContext(em, Talhao::class.java)

    fun analisarNdvi(safraId: Long): List<NdviTalhaoResult> =
        query<Talhao, NdviTalhaoResult>(ctx) {

            val contorno = geomColumn("boundary")
            val ndvi     = raster("ndvi")

            // Recortar NDVI pelo contorno do talhão
            val ndviTalhao = ndvi.stClip(dsl, contorno)

            // Estatísticas do NDVI recortado
            val stats = ndviTalhao.stSummaryStats(dsl)

            // Área do talhão em m²
            val area = contorno.area()

            where { entity.get<Long>("safraId") eq safraId }

            select<NdviTalhaoResult> {
                dto<NdviTalhaoResult> {
                    field(entity.get<Long>("id").alias("talhaoId"))
                    field(stats.mean.alias("ndviMedio"))
                    field(stats.max.alias("ndviMax"))
                    field(stats.min.alias("ndviMin"))
                    field(stats.stddev.alias("ndviStdDev"))
                    field(area.alias("areaTalhaoMq"))
                }
            }
        }
}
```

---

### Exemplo 5 — Retornar entidade JPA (sem DTO)

Quando você precisa das entidades completas, sem projeção.

```kotlin
fun listarAreasQueContem(ponto: Geometry): List<Area> =
    query<Area, Area>(ctx) {

        val input    = toGeometryExpr(ponto)
        val poligono = geomColumn("polygon")

        where { poligono.contains(input).toPredicate() }

        // Retorna as entidades completas
        select<Area> { entity() }
    }
```

---

## Sistema de Dialetos

A partir da versão `0.2.0-alpha`, o Gonexar introduz uma **camada de abstração de dialetos**
que permite suportar diferentes bancos de dados espaciais sem modificar o core do DSL.

### Dialetos disponíveis

| Dialect class | Banco de dados | Geography | Raster | Uso recomendado |
|---|---|:---:|:---:|---|
| `GonexarPostgisDialect` | PostgreSQL + PostGIS | ✓ | ✓ | Produção |
| `GonexarH2GisDialect` | H2 + H2GIS | ✗ | ✗ | Testes de integração |

### Matriz de capacidades

| Operação | PostGIS | H2GIS |
|---|:---:|:---:|
| ST_Intersects, ST_Contains, ST_Within … | ✓ | ✓ |
| ST_Buffer, ST_Simplify, ST_Transform | ✓ | ✓ |
| ST_Distance, ST_Length, ST_Area | ✓ | ✓ |
| ST_Union, ST_Intersection, ST_Difference | ✓ | ✓ |
| ST_LineInterpolatePoint, ST_Azimuth | ✓ | ✓ |
| ST_HausdorffDistance | ✓ | ✓ |
| `geography` cast (métricas geodésicas) | ✓ | ✗ |
| ST_DWithin | ✓ | ✗ |
| Raster (ST_Clip, ST_Slope, ST_Value …) | ✓ | ✗ |

### Testes com H2GIS (sem Docker)

Adicione nas dependências de teste:

```kotlin
// build.gradle.kts
testImplementation("org.orbisgis:h2gis:2.2.3")
testImplementation("com.h2database:h2:2.2.224")
```

Configure o profile de teste:

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.gonexar.dialect.h2gis.GonexarH2GisDialect
spring.jpa.hibernate.ddl-auto=create-drop
```

Habilite as funções espaciais no H2GIS antes dos testes:

```kotlin
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)   // required: allows @BeforeAll on instance method
class MyRepositoryTest {

    @Autowired lateinit var dataSource: DataSource

    @BeforeAll
    fun enableH2GIS() {
        dataSource.connection.use { conn ->
            conn.createStatement().execute("""
                CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL
                FOR "org.h2gis.functions.factory.H2GISFunctions.load";
                CALL H2GIS_SPATIAL();
            """)
        }
    }
}
```

### Implementando um novo dialeto

Para suportar um novo banco (ex: MySQL Spatial), implemente três artefatos:

**1. `SpatialFunctionContributor`** — registra funções SQL:

```kotlin
object MySQLSpatialFunctionContributor : SpatialFunctionContributor {

    override val capabilities: DialectCapabilities
        get() = DialectCapabilities.MYSQL_SPATIAL

    override fun registerFunctions(functionContributions: FunctionContributions) {
        val f = functionContributions.functionRegistry
        val geomRef = BasicTypeReference("geometry", Geometry::class.java, SqlTypes.GEOMETRY)
        f.register("ST_Intersects", StandardSQLFunction("ST_Intersects", StandardBasicTypes.BOOLEAN))
        f.register("ST_Buffer",     StandardSQLFunction("ST_Buffer", geomRef))
        // ...
    }
}
```

**2. `SpatialTypeContributor`** — registra tipos Java/JDBC:

```kotlin
object MySQLSpatialTypeContributor : SpatialTypeContributor {
    override fun registerTypes(typeContributions: TypeContributions, serviceRegistry: ServiceRegistry) {
        typeContributions.typeConfiguration.javaTypeRegistry
            .addDescriptor(JTSGeometryJavaType.GEOMETRY_INSTANCE)
    }
}
```

**3. Dialect Hibernate** — delega aos contributors:

```kotlin
class GonexarMySQLSpatialDialect : MySQLDialect() {
    override fun contributeTypes(tc: TypeContributions, sr: ServiceRegistry) {
        super.contributeTypes(tc, sr)
        MySQLSpatialTypeContributor.registerTypes(tc, sr)
    }
    override fun initializeFunctionRegistry(fc: FunctionContributions) {
        super.initializeFunctionRegistry(fc)
        MySQLSpatialFunctionContributor.registerFunctions(fc)
    }
}
```

Configure:

```properties
spring.jpa.database-platform=org.gonexar.dialect.mysql.GonexarMySQLSpatialDialect
```

---

## Estrutura do projeto

```
src/main/java/org/gonexar/
├── ast/              # AST para SELECT (SelectBuilder, SelectNode)
├── dialect/          # Sistema de dialetos
│   ├── SpatialFunctionContributor.kt  ◄ interface — registro de funções
│   ├── SpatialTypeContributor.kt      ◄ interface — registro de tipos
│   ├── DialectCapabilities.kt         ◄ capabilities por banco
│   ├── GonexarPostgisDialect.kt       ◄ dialeto de produção
│   ├── postgis/
│   │   ├── PostgisFunctionContributor.kt
│   │   └── PostgisTypeContributor.kt
│   └── h2gis/
│       ├── GonexarH2GisDialect.kt
│       ├── H2GisFunctionContributor.kt
│       └── H2GisTypeContributor.kt
├── expression/       # SpatialExpr<T>, NumericExpr<T>
├── operator/         # Mixin interfaces de operadores
├── query/            # Funções de consulta (Intersect.kt)
├── repository/       # GonexarSpatialRepository
├── spatial/          # Core do DSL (SpatialDslContainer, QueryContainer …)
└── type/             # Sistema de tipos Raster
```
