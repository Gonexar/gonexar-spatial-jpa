# Changelog

Todas as mudanças importantes deste projeto serão documentadas aqui.

O formato segue o padrão de versão semântica, porém enquanto estivermos na fase `x.y.0-alpha`
a API ainda pode mudar com maior frequência.

---

## [1.2.0-alpha] - 2026-06-29

### Novidades

#### Abstração de Dialetos (`org.gonexar.dialect`)

Introduce uma camada de abstração que desacopla o DSL do PostgreSQL/PostGIS e permite suporte a múltiplos bancos de dados sem alterar o core da biblioteca.

- **`SpatialFunctionContributor`** — interface que define o contrato para registro de funções SQL por dialeto, junto com as `DialectCapabilities` que o banco suporta
- **`SpatialTypeContributor`** — interface para registro de descritores Java/JDBC por dialeto
- **`DialectCapabilities`** — value object com flags de capacidade por banco; dialetos precisam optar explicitamente por cada operação avançada
- **`UnsupportedSpatialOperationException`** — exceção tipada lançada quando uma operação DSL não é suportada pelo dialeto ativo, substituindo falhas SQL opacas
- **`PostgisFunctionContributor`** / **`PostgisTypeContributor`** — implementações PostGIS extraídas do dialeto monolítico
- **`GonexarH2GisDialect`** + **`H2GisFunctionContributor`** + **`H2GisTypeContributor`** — suporte completo ao H2GIS para testes de integração sem Docker ou servidor PostgreSQL

Presets de capacidade disponíveis:

| Preset | Banco | Geography | Raster | Transform | Collect | LineMerge | Hausdorff |
|---|---|:---:|:---:|:---:|:---:|:---:|:---:|
| `DialectCapabilities.POSTGIS` | PostgreSQL + PostGIS | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| `DialectCapabilities.H2GIS` | H2 + H2GIS | ✗ | ✗ | ✓ | ✓ | ✓ | ✓ |
| `DialectCapabilities.MYSQL_SPATIAL` | MySQL Spatial | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ |

#### Raster — `stSummaryStats` funcional

A operação `stSummaryStats()` foi reescrita do zero. A implementação anterior gerava SQL inválido em runtime. Agora registra seis funções padrão via `registerPattern` do Hibernate 6 que expandem para o acesso correto ao composite type PostgreSQL:

```sql
-- SQL gerado por cada campo projetado
(ST_SummaryStats(rast)).count
(ST_SummaryStats(rast)).sum
(ST_SummaryStats(rast)).mean
(ST_SummaryStats(rast)).stddev
(ST_SummaryStats(rast)).min
(ST_SummaryStats(rast)).max
```

#### Documentação

- README completamente reescrito com 10 seções: _O que é_, _Para que serve_, _No que se destaca_, _Requisitos e compatibilidade_, _Como instalar_, _Conceitos fundamentais_, _Operadores disponíveis_ (referência completa de todos os grupos), _Exemplos de implementação_ (5 exemplos end-to-end anotados), _Sistema de dialetos_ e _Estrutura do projeto_
- `docs/dialect-architecture.md` — diagrama de componentes, tabela de wire formats por banco, checklist para adicionar novo dialeto

---

### Correções

#### `DialectCapabilities` — flags opcionais com padrão incorreto

**Arquivo:** `src/main/java/org/gonexar/dialect/DialectCapabilities.kt`

Os campos `supportsTransform`, `supportsCollect`, `supportsLineMerge` e `supportsHausdorff` tinham default `true`, fazendo com que novos dialetos declarassem suporte a operações que não implementavam. Alterados para `false` — dialetos agora devem optar explicitamente por cada capacidade.

#### `PostgisFunctionContributor` — tipos de retorno incorretos

**Arquivo:** `src/main/java/org/gonexar/dialect/postgis/PostgisFunctionContributor.kt`

`ST_GeometryType` e `ST_Dimension` estavam registradas com `geometryTypeRef` como tipo de retorno. Corrigido para `StandardBasicTypes.STRING` e `StandardBasicTypes.INTEGER` respectivamente, alinhando com os tipos reais que o PostGIS retorna.

#### `RasterUserType` — quatro bugs no sistema de tipos Raster

**Arquivo:** `src/main/java/org/gonexar/type/RasterUserType.kt`

| # | Problema | Impacto | Correção |
|---|---|---|---|
| 1 | `getSqlType()` retornava `Types.BINARY` | Escrita em colunas `raster` falhava com _"expression is of type bytea"_ | Alterado para `Types.OTHER` |
| 2 | `deepCopy()` hardcodava `srid = 0` | SRID perdido em todo snapshot de dirty-checking e cache L1 | Alterado para `value.srid` |
| 3 | `equals()`/`hashCode()` ignoravam o SRID | Mudança apenas de SRID não era detectada como modificação | Delegado para `Raster.equals()` |
| 4 | `disassemble()` serializava apenas `bytes` | SRID perdido em round-trips de cache L2 (Ehcache, Redis, etc.) | Serializa `Pair<ByteArray, Int?>` preservando ambos |

#### `RasterJavaType` — inconsistência de `areEqual` e erro de compilação

**Arquivo:** `src/main/java/org/gonexar/type/RasterJavaType.kt`

- `areEqual()` comparava apenas bytes, ignorando SRID — inconsistente com `Raster.equals()`. Corrigido delegando para `Raster.equals()`
- `unwrap()` passava `type` nullable para `UnknownUnwrapTypeException` que espera `Class<*>` não-nulo. Corrigido com `type ?: return null`

#### `registerPattern` — mismatch de tipo

**Arquivo:** `src/main/java/org/gonexar/dialect/postgis/PostgisFunctionContributor.kt`

`registerPattern` em Hibernate 6 espera `BasicType<*>`, não `BasicTypeReference<*>`. As chamadas para registrar as funções `raster_stat_*` falhavam em compilação. Corrigido resolvendo os tipos via `functionContributions.typeConfiguration.basicTypeRegistry.resolve(...)`.

#### README — exemplo H2GIS sem anotação de lifecycle

`@BeforeAll` em Kotlin requer `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` ou `@JvmStatic` em `companion object`. O exemplo no README estava incorreto e causaria `TestInstantiationException` em runtime. Corrigido adicionando a anotação.

#### `GonexarPostgisDialect` — KDoc incorreto

O KDoc orientava a "estender `PostgisFunctionContributor`", mas esse é um Kotlin `object` e não pode ser estendido. Corrigido com o padrão real: implementar `SpatialFunctionContributor` e subclassificar `GonexarPostgisDialect`.

---

### Dependências atualizadas

| Biblioteca | Versão anterior | Versão nova |
|---|---|---|
| Kotlin | 1.9.22 | **2.2.0** |
| `org.hibernate.orm:hibernate-core` | 6.4.1.Final | **6.6.18.Final** |
| `org.hibernate.orm:hibernate-spatial` | 6.4.1.Final | **6.6.18.Final** |
| `jakarta.persistence:jakarta.persistence-api` | 3.1.0 | **3.2.0** |
| `net.postgis:postgis-jdbc` | 2.5.0 | **2024.1.0** |
| `org.locationtech.jts:jts-core` | 1.19.0 | **1.20.0** |
| `com.fasterxml.jackson.core:jackson-databind` | 2.15.2 | **2.21.1** |
| `org.projectlombok:lombok` | 1.18.30 | **1.18.38** |
| `org.postgresql:postgresql` | 42.7.7 | 42.7.7 _(sem alteração)_ |

> Kotlin 2.2.0 ativa o compilador K2 (GA desde 2.0). O plugin `kotlin("plugin.jpa")` é compatível. Hibernate 6.6 requer Jakarta Persistence 3.2 — as duas atualizações andam em conjunto.

---

## [0.1.0-alpha] - 2025-12-01

### Status
Prévia técnica (Alpha).  
API ainda pode mudar entre versões.

### Added
- Núcleo inicial do DSL espacial baseado em Kotlin + JPA Criteria API
- `SpatialDslContext` e `CriteriaDslContext` — motores internos da construção de queries
- `SpatialDslContainer` — camada pública usada pelos desenvolvedores
- Suporte a operadores geométricos:
    - `buffer(distance)`
    - `intersection(other)`
    - `intersects(other)`
    - `closestPoint(other)`
    - `distance(other)`
    - `lengthGeography()`
    - `lineMerge()`
    - `asText()`
- Operadores numéricos e estatísticos:
    - `percentageInsideIntersection()`
    - `bearingAlongRoute()`
- Infraestrutura para projeção:
    - `resultProjection()`
    - `dto { field(...) }`
- Suporte a `queryContainer()` para compor consultas espaciais complexas usando DSL
- Exemplo funcional completo: **RouteAnalysis** (interseção de rota + área + métricas)
- Estrutura modular para operadores: `GeometryOperator`, `TopologyOperator`, etc.

### Changed
- Melhorias internas no aliasing automático de expressões
- Normalização de geometria com `lineMerge()` para MultiLineString
- Correções de cast para Geography em funções geoespaciais

### Known Limitations
- Documentação detalhada ainda em desenvolvimento
- Testes automatizados mínimos (apenas smoke tests internos)
- Sem pipeline CI/CD ainda
- Alguns operadores raster ainda não implementados
