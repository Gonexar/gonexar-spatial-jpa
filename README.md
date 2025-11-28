# Gonexar — Spatial DSL for Kotlin + JPA + PostGIS
Versão: **0.1.0-alpha**

O **Gonexar Spatial DSL** é uma biblioteca que traz o poder do **PostGIS** para dentro do
ecossistema **Kotlin + Spring + JPA Criteria API**, permitindo criar análises espaciais complexas
com poucas linhas de código, sem escrever SQL manual.

> Esta é a versão **alpha/preview**. A API pública ainda está sujeita a mudanças.

---

## ✨ Por que o Gonexar existe?

PostGIS é extremamente poderoso — mas escrever SQL espacial realista significa lidar com
funções complexas (`ST_Buffer`, `ST_Intersection`, `ST_LineLocatePoint`, `ST_ClosestPoint`, etc.),
com geography casts, alias complicados e dezenas de expressões.

O Gonexar resolve isso com:

- Sintaxe fluente em Kotlin
- Tipagem forte (Kotlin + Criteria API)
- DSL que compõe operadores PostGIS automaticamente
- Integração transparente com JPA e repositórios
- Resultados mapeados direto para DTOs ou entidades

---

## 🚀 Quickstart

### 1. Instale localmente
```bash
./gradlew publishToMavenLocal
ou
./gradlew publish