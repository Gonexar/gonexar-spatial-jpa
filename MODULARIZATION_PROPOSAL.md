# Proposta de Modularização — gonexar-spatial-jpa

**Branch:** `proposal/modularization`  
**Base:** `developer`  
**Versão alvo:** `0.2.0-alpha`

---

## Motivação

A biblioteca hoje entrega um único artefato (`gonexar-spatial-jpa`) que carrega em runtime **tanto** o driver PostGIS/PostgreSQL **quanto** as dependências H2GIS, mesmo quando o consumidor usa apenas um dos dois. Isso viola o princípio de menor superfície de dependência e gera conflitos em projetos que gerenciam drivers de banco com escopos distintos (ex.: PostGIS em `implementation`, H2GIS em `testImplementation`).

A separação em módulos resolve:

- **Dependências mínimas por dialeto** — quem usa PostGIS não carrega H2GIS e vice-versa.
- **Separação de contrato vs. implementação** — o `core` define as interfaces; os módulos de dialeto são plugins intercambiáveis.
- **Testabilidade isolada** — H2GIS pode ser declarado `testImplementation` sem contaminar o classpath de produção.
- **Extensibilidade** — um terceiro dialeto (ex.: Oracle Spatial, MySQL Spatial) pode ser adicionado sem alterar o `core`.

---

## Estrutura proposta

```
gonexar-spatial-jpa/               ← root project (build.gradle.kts + settings.gradle.kts)
│
├── gonexar-spatial-core/          ← artefato: com.gonexar:gonexar-spatial-core
│   └── src/main/java/org/gonexar/
│       ├── ast/                   (SelectNode, SelectBuilder, ExprNode, EntityNode, NestedDtoNode)
│       ├── expression/            (SpatialExpr, NumericExpr)
│       ├── operator/              (GeometryOperator, TopologyOperator, SpatialOperator,
│       │                           MatrixOperator, MathOperator, TemporalOperator)
│       ├── spatial/               (CriteriaDslContext, SpatialDslContext, SpatialDslContainer,
│       │                           SpatialContext, QueryContainer)
│       ├── repository/            (GonexarSpatialRepository)
│       ├── query/                 (Intersect)
│       ├── type/                  (Raster, RasterJavaType, RasterUserType, RasterStatsExpr)
│       └── dialect/               (DialectCapabilities, SpatialFunctionContributor,
│                                   SpatialTypeContributor)   ← apenas interfaces/contratos
│
├── gonexar-spatial-postgis/       ← artefato: com.gonexar:gonexar-spatial-postgis
│   └── src/main/java/org/gonexar/dialect/
│       ├── GonexarPostgisDialect.kt
│       ├── postgis/
│       │   ├── PostgisFunctionContributor.kt
│       │   └── PostgisTypeContributor.kt
│       └── (futuros: PostgisRasterContributor, etc.)
│
└── gonexar-spatial-h2gis/        ← artefato: com.gonexar:gonexar-spatial-h2gis
    └── src/main/java/org/gonexar/dialect/
        ├── h2gis/
        │   ├── GonexarH2GisDialect.kt
        │   ├── H2GisFunctionContributor.kt
        │   └── H2GisTypeContributor.kt
        └── (futuros: H2GisSpatialExtension, etc.)
```

---

## Grafo de dependências

```
gonexar-spatial-postgis
    └── api → gonexar-spatial-core
                └── api → jts-core
                └── api → hibernate-core
                └── api → hibernate-spatial
                └── api → jakarta.persistence-api
                └── api → jackson-databind
    └── api → postgis-jdbc
    └── api → postgresql

gonexar-spatial-h2gis
    └── api → gonexar-spatial-core   (mesmo grafo acima)
    └── implementation → h2gis:2.2.3
```

---

## Mapeamento de arquivos: de onde para onde

### Permanecem no `core`

| Arquivo atual | Destino |
|---|---|
| `dialect/DialectCapabilities.kt` | `gonexar-spatial-core/.../dialect/` |
| `dialect/SpatialFunctionContributor.kt` | `gonexar-spatial-core/.../dialect/` |
| `dialect/SpatialTypeContributor.kt` | `gonexar-spatial-core/.../dialect/` |
| `expression/SpatialExpr.kt` | `gonexar-spatial-core/.../expression/` |
| `expression/NumericExpr.kt` | `gonexar-spatial-core/.../expression/` |
| `operator/GeometryOperator.kt` | `gonexar-spatial-core/.../operator/` |
| `operator/TopologyOperator.kt` | `gonexar-spatial-core/.../operator/` |
| `operator/SpatialOperator.kt` | `gonexar-spatial-core/.../operator/` |
| `operator/MatrixOperator.kt` | `gonexar-spatial-core/.../operator/` |
| `operator/MathOperator.kt` | `gonexar-spatial-core/.../operator/` |
| `operator/TemporalOperator.kt` | `gonexar-spatial-core/.../operator/` |
| `spatial/CriteriaDslContext.kt` | `gonexar-spatial-core/.../spatial/` |
| `spatial/SpatialDslContext.kt` | `gonexar-spatial-core/.../spatial/` |
| `spatial/SpatialDslContainer.kt` | `gonexar-spatial-core/.../spatial/` |
| `spatial/SpatialContext.kt` | `gonexar-spatial-core/.../spatial/` |
| `spatial/QueryContainer.kt` | `gonexar-spatial-core/.../spatial/` |
| `ast/SelectNode.kt` | `gonexar-spatial-core/.../ast/` |
| `ast/SelectBuilder.kt` | `gonexar-spatial-core/.../ast/` |
| `ast/ExprNode.kt` | `gonexar-spatial-core/.../ast/` |
| `ast/EntityNode.kt` | `gonexar-spatial-core/.../ast/` |
| `ast/NestedDtoNode.kt` | `gonexar-spatial-core/.../ast/` |
| `type/Raster.kt` | `gonexar-spatial-core/.../type/` |
| `type/RasterJavaType.kt` | `gonexar-spatial-core/.../type/` |
| `type/RasterUserType.kt` | `gonexar-spatial-core/.../type/` |
| `type/RasterStatsExpr.kt` | `gonexar-spatial-core/.../type/` |
| `repository/GonexarSpatialRepository.kt` | `gonexar-spatial-core/.../repository/` |
| `query/Intersect.kt` | `gonexar-spatial-core/.../query/` |

### Movem para `gonexar-spatial-postgis`

| Arquivo atual | Destino |
|---|---|
| `dialect/GonexarPostgisDialect.kt` | `gonexar-spatial-postgis/.../dialect/` |
| `dialect/postgis/PostgisFunctionContributor.kt` | `gonexar-spatial-postgis/.../dialect/postgis/` |
| `dialect/postgis/PostgisTypeContributor.kt` | `gonexar-spatial-postgis/.../dialect/postgis/` |

### Movem para `gonexar-spatial-h2gis`

| Arquivo atual | Destino |
|---|---|
| `dialect/h2gis/GonexarH2GisDialect.kt` | `gonexar-spatial-h2gis/.../dialect/h2gis/` |
| `dialect/h2gis/H2GisFunctionContributor.kt` | `gonexar-spatial-h2gis/.../dialect/h2gis/` |
| `dialect/h2gis/H2GisTypeContributor.kt` | `gonexar-spatial-h2gis/.../dialect/h2gis/` |

> **Nenhuma alteração de package** é necessária. Os pacotes `org.gonexar.dialect`, `org.gonexar.dialect.postgis` e `org.gonexar.dialect.h2gis` já existem e apenas mudam de módulo físico.

---

## Como os consumidores declaram a dependência

### Produção com PostgreSQL + PostGIS

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.gonexar:gonexar-spatial-postgis:0.2.0-alpha")
}
```

### Produção com PostgreSQL + PostGIS, testes com H2GIS

```kotlin
dependencies {
    implementation("com.gonexar:gonexar-spatial-postgis:0.2.0-alpha")
    testImplementation("com.gonexar:gonexar-spatial-h2gis:0.2.0-alpha")
}
```

### Biblioteca que apenas consome as interfaces do core

```kotlin
dependencies {
    api("com.gonexar:gonexar-spatial-core:0.2.0-alpha")
}
```

---

## Impacto em projetos que já usam `0.1.0-alpha`

O artefato `gonexar-spatial-jpa` (antigo) não será mais publicado a partir de `0.2.0-alpha`. Projetos existentes precisam substituir:

```kotlin
// Antes
implementation("com.gonexar:gonexar-spatial-jpa:0.1.0-alpha")

// Depois (escolher o(s) dialeto(s) necessário(s))
implementation("com.gonexar:gonexar-spatial-postgis:0.2.0-alpha")
// + opcionalmente:
testImplementation("com.gonexar:gonexar-spatial-h2gis:0.2.0-alpha")
```

**Não há mudança em nenhuma API pública** — packages, nomes de classe e assinaturas de método permanecem idênticos.

---

## Decisões de design

### Por que `type/` (Raster) fica no `core`?

`RasterUserType` e `RasterJavaType` dependem apenas de `hibernate-core` e `jakarta.persistence-api`, ambos já no core. O tipo `Raster` é uma abstração agnóstica de dialeto — PostGIS usa BYTEA, mas futuros dialetos poderiam usar outros formatos. Manter no core evita que o módulo PostGIS vaze um tipo concreto de dados.

### Por que `query/Intersect.kt` fica no `core`?

`Intersect` implementa um padrão de consulta usando apenas a API do `SpatialDslContainer`, sem qualquer referência a dialeto. Ele funciona tanto em PostGIS quanto em H2GIS. Se futuramente surgir uma operação exclusiva de PostGIS (ex.: `ST_DWithin` com geografia), ela ficará em `gonexar-spatial-postgis/query/`.

### Por que não criar um BOM (Bill of Materials)?

O projeto tem apenas dois dialetos e dependências simples. Um BOM seria overhead prematuro. Pode ser revisado se o número de módulos crescer (ex.: Oracle Spatial, MySQL Spatial).

### Por que H2GIS usa `implementation` em vez de `api`?

H2GIS não faz parte do contrato público do módulo — é um detalhe de implementação do dialeto. Consumidores que precisam do driver H2GIS para criar conexões de teste devem declará-lo diretamente como `testImplementation`.

---

## Checklist de implementação

- [ ] Criar estrutura de diretórios dos três módulos
- [ ] Mover arquivos de fonte para os módulos corretos (sem alterar packages)
- [ ] Remover `src/` do root project
- [ ] Atualizar `settings.gradle.kts` (inclui os três módulos) ✅ feito nesta branch
- [ ] Reescrever `build.gradle.kts` root com `subprojects {}` ✅ feito nesta branch
- [ ] Criar `gonexar-spatial-core/build.gradle.kts` ✅ feito nesta branch
- [ ] Criar `gonexar-spatial-postgis/build.gradle.kts` ✅ feito nesta branch
- [ ] Criar `gonexar-spatial-h2gis/build.gradle.kts` ✅ feito nesta branch
- [ ] Adicionar testes de integração em `gonexar-spatial-postgis` e `gonexar-spatial-h2gis`
- [ ] Atualizar README com novo guia de dependências
- [ ] Atualizar CHANGELOG com breaking change da versão 0.2.0-alpha
- [ ] Publicar artefatos separados no GitHub Packages
