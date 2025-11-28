# Changelog

Todas as mudanças importantes deste projeto serão documentadas aqui.

O formato segue o padrão de versão semântica, porém enquanto estivermos na fase `0.x`
a API ainda pode mudar com maior frequência.

---

## [0.1.0-alpha] - 2025-11-28
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
