# Gonexar Dialect Architecture

## Motivation

The original `GonexarPostgisDialect` contained all spatial type registrations and
function registrations directly in two override methods. This worked for a single
database but made it impossible to:

1. Add support for another spatial database without duplicating code.
2. Unit-test function registration in isolation.
3. Communicate upfront what a given database supports (leading to cryptic SQL errors
   at runtime for unsupported operations).

This document describes the contributor pattern introduced to solve these problems.

---

## Core abstractions

### `SpatialFunctionContributor`

```
interface SpatialFunctionContributor {
    val capabilities: DialectCapabilities
    fun registerFunctions(functionContributions: FunctionContributions)
}
```

Responsibility: Register every SQL function the target database supports into
Hibernate's `FunctionContributions.functionRegistry`. Each registration makes a
function available via `CriteriaBuilder.function("ST_Xxx", ...)`.

### `SpatialTypeContributor`

```
interface SpatialTypeContributor {
    fun registerTypes(typeContributions: TypeContributions, serviceRegistry: ServiceRegistry)
}
```

Responsibility: Register Java type descriptors (`JavaType`) and JDBC type
descriptors (`JdbcType`) into Hibernate's type registries. This determines how
spatial column values are serialised/deserialised between the database wire format
and JVM types.

### `DialectCapabilities`

```
data class DialectCapabilities(
    val supportsGeography: Boolean,
    val supportsRaster: Boolean,
    val supportsTransform: Boolean = true,
    val supportsCollect: Boolean = true,
    val supportsLineMerge: Boolean = true,
    val supportsHausdorff: Boolean = true,
)
```

Responsibility: Advertise what a given database supports. Attached to each
`SpatialFunctionContributor` via the `capabilities` property. The DSL can consult
it before invoking dialect-specific operations, throwing
`UnsupportedSpatialOperationException` with a clear message instead of letting the
database driver produce an opaque SQL error.

---

## Component diagram

```
┌─────────────────────────────────────────────────────────────┐
│  Hibernate bootstrap                                        │
│                                                             │
│  Dialect.contributeTypes()         ──► SpatialTypeContributor
│  Dialect.initializeFunctionRegistry() ──► SpatialFunctionContributor
└─────────────────────────────────────────────────────────────┘
                        │
          ┌─────────────┴──────────────┐
          │                            │
 ┌────────▼────────┐          ┌────────▼────────┐
 │  PostGIS        │          │  H2GIS          │
 │                 │          │                 │
 │ PostgisType-    │          │ H2GisType-      │
 │ Contributor     │          │ Contributor     │
 │                 │          │                 │
 │ PostgisFunction-│          │ H2GisFunction-  │
 │ Contributor     │          │ Contributor     │
 │                 │          │                 │
 │ capabilities =  │          │ capabilities =  │
 │ POSTGIS         │          │ H2GIS           │
 └────────┬────────┘          └────────┬────────┘
          │                            │
 ┌────────▼────────┐          ┌────────▼────────┐
 │GonexarPostgis-  │          │GonexarH2Gis-    │
 │Dialect          │          │Dialect          │
 │extends          │          │extends          │
 │PostgreSQLDialect│          │H2Dialect        │
 └─────────────────┘          └─────────────────┘
```

---

## Wire format differences between databases

Understanding how each database serialises geometries over JDBC is important when
implementing a new `SpatialTypeContributor`.

| Database | Wire format | JavaType | JdbcType |
|---|---|---|---|
| PostGIS | EWKB (binary, SRID prefix) | `JTSGeometryJavaType` | `PGGeometryJdbcType.INSTANCE_WKB_2` |
| H2GIS | WKT (text) | `JTSGeometryJavaType` | H2Dialect built-in |
| MySQL Spatial | WKB (binary, 4-byte SRID prefix) | `JTSGeometryJavaType` | Custom `MySQLGeometryJdbcType` |
| Oracle Spatial | SDO_GEOMETRY (proprietary) | Custom | Custom |

---

## Capability-aware DSL patterns

When an operator is only available on some databases, guard it with `DialectCapabilities`.

### Pattern A — throw at DSL invocation time

For operations with no cross-database equivalent (raster, geography):

```kotlin
fun SpatialExpr<Raster>.stClip(
    dsl: SpatialDslContext<*, *>,
    geom: SpatialExpr<Geometry>
): SpatialExpr<Raster> {
    // future: check dsl.capabilities.supportsRaster
    return dsl.register(
        dsl.autoAlias(expr),
        dsl.cb.function("ST_Clip", Raster::class.java, expr, geom.expr)
    )
}
```

### Pattern B — dialect-aware fallback

For operations that have equivalents (e.g., geodesic buffer vs planar buffer):

```kotlin
fun SpatialExpr<Geometry>.buffer(distance: Double): SpatialExpr<Geometry> {
    return if (supportsGeography) {
        // PostGIS: metre-accurate geodesic buffer via geography cast
        val geog = dsl.cb.function("geography", Any::class.java, this.expr)
        val buf  = dsl.cb.function("ST_Buffer", Geometry::class.java, geog, dsl.cb.literal(distance))
        val geom = dsl.cb.function("geometry", Geometry::class.java, buf)
        dsl.register(dsl.autoAlias(geom), geom)
    } else {
        // H2GIS / MySQL: planar buffer (units depend on CRS)
        val buf = dsl.cb.function("ST_Buffer", Geometry::class.java, this.expr, dsl.cb.literal(distance))
        dsl.register(dsl.autoAlias(buf), buf)
    }
}
```

Pattern B is preferred for operators the application will use across multiple environments
(e.g., production PostGIS + test H2GIS). Pattern A is acceptable for PostGIS-exclusive
operations (raster, geography-only metrics) that tests simply won't exercise.

---

## Checklist for adding a new dialect

- [ ] Create `<Db>FunctionContributor : SpatialFunctionContributor`
  - [ ] Set `capabilities` accurately
  - [ ] Register every function the application requires
  - [ ] Do NOT register functions the target database does not support
- [ ] Create `<Db>TypeContributor : SpatialTypeContributor`
  - [ ] Register `JTSGeometryJavaType` for geometry column mapping
  - [ ] Register the appropriate `JdbcType` for the database wire format
  - [ ] Register any custom `UserType` (e.g., raster equivalents)
- [ ] Create `Gonexar<Db>Dialect : <ParentHibernateDialect>()`
  - [ ] Delegate `contributeTypes` to `<Db>TypeContributor`
  - [ ] Delegate `initializeFunctionRegistry` to `<Db>FunctionContributor`
- [ ] Add documentation in `README.md`
- [ ] Add integration tests using the new dialect

---

## Future work

| Item | Priority | Notes |
|---|:---:|---|
| Capability check at DSL build time | High | Guard `buffer()`, `slope()` etc. against unsupported dialects |
| Oracle Spatial dialect | Medium | SDO_GEOMETRY requires a custom JdbcType |
| MySQL Spatial dialect | Medium | WKB with 4-byte SRID prefix; no `ST_Transform`, no `ST_HausdorffDistance` |
| Spring Boot AutoConfiguration | Medium | Detect dialect on classpath, configure automatically |
| postgis-jdbc upgrade to 2023.1+ | Low | Compatibility with PostGIS 3.x features |
