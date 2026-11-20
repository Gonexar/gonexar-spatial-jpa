# Changelog

Todas as mudanças importantes deste projeto serão documentadas aqui.

O formato segue o padrão de versão semântica, porém enquanto estivermos na fase `0.x`
a API ainda pode mudar com maior frequência.

---

## [0.3.0-alpha] - Não publicada
### Status
Prévia técnica (Alpha). Primeira versão publicada no Maven Central.
API ainda pode mudar entre versões.

### Changed (breaking)
- Pacote base renomeado de `org.gonexar` para `com.gonexar`, alinhado ao groupId.
- `GonexarPostgisDialect` mudou para `com.gonexar.dialect.postgis.GonexarPostgisDialect`
  (antes `org.gonexar.dialect.GonexarPostgisDialect`). O pacote `com.gonexar.dialect`
  agora existe só no core, sem split package entre módulos.
- Projeto dividido em três artefatos: `gonexar-spatial-core`, `gonexar-spatial-postgis`
  e `gonexar-spatial-h2gis`. O artefato único `gonexar-spatial-jpa` não é mais publicado.
- O driver `org.postgresql:postgresql` passou a ser dependência de runtime do módulo
  PostGIS, e não mais de compilação.

### Fixed
- Funções que retornam geometria (`intersection`, `union`, `closestPoint`, `startPoint`,
  `simplify`, `transform` …) falhavam com `NullPointerException` ao serem projetadas em DTOs
  (`select { field(...) }`), nos dois dialetos. O tipo de retorno agora é resolvido pelo
  registro de tipos do Hibernate.
- `select<E> { entity() }` (e o helper `intersects` de `GonexarSpatialRepository`) falhava
  procurando um construtor `E(E)`. Agora seleciona a própria entidade.
- H2GIS: `buffer`, `distance`, `area` e `lengthGeography` falhavam porque a DSL usa casts
  `geography(...)`/`geometry(...)`. No H2GIS esses casts viram no-op e as operações rodam
  em unidades planas.
- H2GIS: removidas as funções `ST_LineLocatePoint` e `ST_HausdorffDistance`, que não existem
  no H2GIS 2.2; `DialectCapabilities.H2GIS.supportsHausdorff` passa a ser `false`.

### Removed
- Dependências não utilizadas: `jackson-databind` (antes exposta como `api` no core) e Lombok.

### Build
- Publicação no Maven Central (Central Portal) com POM completo, assinatura GPG,
  jar de fontes e javadoc gerado pelo Dokka.
- Workflows de CI (build em PRs) e de publicação (por tag `v<versão>`).
- Toolchain Java 17 provisionado automaticamente (foojay resolver).
- Suíte de testes: unitários no core, integração com H2GIS e com PostGIS via Testcontainers,
  e verificação de que toda função registrada nos dialetos existe no banco.
- Cobertura com Kover agregada nos três módulos, com piso de 70% de linhas no CI.

---

## [0.2.0-alpha] - 2026-08-02
### Status
Prévia técnica (Alpha).
API ainda pode mudar entre versões.

### Added
- Sobrecarga não-reificada de `query(ctx, resultClass, block)`, que recebe `Class<R>`
  explicitamente. Permite chamar a DSL espacial a partir de código que não pode reificar
  `R` em tempo de compilação (ex.: implementações de interface, fronteiras genéricas).

### Changed
- `query(ctx, block)` (reificada) passou a delegar para a sobrecarga não-reificada,
  eliminando a duplicação de lógica de construção da `CriteriaQuery` entre as duas versões.

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

### Next Steps (v0.2.0-alpha)
- Documentação avançada de DSL
- Testes unitários para operadores
- Suporte completo a Raster (NDVI, Clip, Summary Stats)
- Otimização de DSL builder
- Publicação em Maven Central
