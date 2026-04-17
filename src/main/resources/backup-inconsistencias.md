# Informe de Inconsistencias — BackupService vs Esquema de Base de Datos

**Fecha de análisis:** 2026-04-16
**Archivo analizado:** `columns_202604162122.html` (export `information_schema.columns` — schema `public`)
**Servicio auditado:** `BackupService.java`
**Entidades:** `package com.infinitesoft.pos_relational_data_service.entities`

---

## 1. Tablas presentes en la BD que NO estaban en el backup (CORREGIDO)

Las siguientes 19 tablas existían en la base de datos pero no tenían hoja en el Excel de backup.
Todas fueron agregadas en esta actualización:

| Tabla BD | Entidad JPA | Hoja Excel agregada |
|---|---|---|
| `client` | `Client` | Clientes |
| `company` | `Company` | Company |
| `metodo_pago` | `MetodoPago` | Metodo Pago |
| `recibo` | `Recibo` | Recibo |
| `recibo_detalle` | `ReciboDetalle` | Recibo Detalle |
| `recibo_detalle_historico` | `ReciboDetalleHistorico` | Recibo Detalle Historico |
| `edicion_recibo` | `EdicionRecibo` | Edicion Recibo |
| `edicion_recibo_detalle` | `EdicionReciboDetalle` | Edicion Recibo Detalle |
| `egreso` | `Egreso` | Egreso |
| `proveedor` | `Proveedor` | Proveedor |
| `tipo_egreso` | `TipoEgreso` | Tipo Egreso |
| `sesion` | `Sesion` | Sesion |
| `ticket` | `Ticket` | Ticket |
| `ticket_recibo` | `TicketRecibo` | Ticket Recibo |
| `flujo_dinero` | `FlujoDinero` | Flujo Dinero |
| `estadistica_fin` | `EstadisticaFin` | Estadistica Fin |
| `tipo_resultado_fin` | `TipoResultadoFin` | Tipo Resultado Fin |
| `cargue_productos` | `CargueProducto` | Cargue Productos |
| `cargue_producto_conflictos` | `CargueProductoConflicto` | Cargue Prod Conflictos |

---

## 2. Campo faltante en tabla ya existente (CORREGIDO)

| Tabla | Campo BD | Campo JPA | Backup anterior | Estado |
|---|---|---|---|---|
| `producto` | `precio_unidad` | `precioUnidad` (Double) | ❌ No incluido | ✅ Agregado como columna "Precio Unidad" |

---

## 3. Tabla en BD sin entidad JPA (@Entity) — Requiere atención

| Tabla BD | Situación | Impacto |
|---|---|---|
| `tipo_conflicto` | Mapeada como **enum Java** `TipoConflicto` (sin `@Entity`). No existe repositorio JPA. | No se puede consultar vía Spring Data. La tabla tiene `id` y `nombre`. Si se requiere en el backup, se debe crear una entidad `@Entity` con su repositorio. |

**Recomendación:** Crear entidad `TipoConflicto.java` con `@Entity @Table(name = "tipo_conflicto")` y su `TipoConflictoRepository` si se desea incluir la tabla en backups futuros. El enum actual puede conservarse para uso interno.

---

## 4. Entidades JPA sin tabla en el schema `public` — Sin impacto directo en backup

| Entidad JPA | Tabla mapeada | Situación |
|---|---|---|
| `UsuarioPerfil` | `security.usuario_perfil` | Pertenece al schema **`security`**, no al schema `public`. No aparece en el export. No aplica al backup del schema público. |
| `MigracionProducto` | `migracion_productos` | Tabla **no encontrada** en el export del schema `public`. Puede haberse eliminado o estar en otro schema. |
| `MigracionProductoConflicto` | `migracion_producto_conflictos` | Tabla **no encontrada** en el export del schema `public`. Misma situación que `MigracionProducto`. |

---

## 5. Discrepancias entre la definición JPA y la estructura real de la BD

### 5.1 Tipo de dato del campo `id` en varias entidades

| Tabla BD | Tipo en BD | Tipo en JPA | Estado |
|---|---|---|---|
| `egreso` | `integer` (32 bits) | `Long` (64 bits) | ⚠️ Inconsistencia de tipo. Funciona en PostgreSQL pero puede ser confuso. |
| `corte_venta` | `integer` (32 bits) | `Long` (64 bits) | ⚠️ Inconsistencia de tipo. Ídem anterior. |

> **Nota:** PostgreSQL acepta mapeos `integer → Long` en JPA sin error de ejecución, pero el tipo en la entidad debería reflejar fielmente la columna BD (`Integer`) para evitar confusión futura.

### 5.2 Longitud de columna `nombre` en `tipo_egreso`

| Tabla BD | Columna | Longitud BD | Longitud JPA (`@Column(length=...)`) |
|---|---|---|---|
| `tipo_egreso` | `nombre` | `varchar(150)` | `length = 100` |

> **Riesgo:** Si se insertan registros con nombre de entre 101 y 150 caracteres directamente en BD, JPA no los truncará al leer, pero sí al escribir, generando un `DataException`. Se recomienda corregir a `length = 150`.

### 5.3 Tipo `timestamp with time zone` vs `LocalDateTime` en `producto`

| Tabla BD | Columna | Tipo en BD | Tipo en JPA |
|---|---|---|---|
| `producto` | `fecha_creacion` | `timestamp with time zone` | `LocalDateTime` |

> **Riesgo:** `LocalDateTime` no tiene información de zona horaria. En entornos con JVM en zona diferente a UTC puede haber desplazamiento de horas. Se recomienda usar `OffsetDateTime` o `ZonedDateTime` para esta columna.

### 5.4 Columna `description` en `company` sin longitud explícita

| Tabla BD | Columna | Tipo en BD | Definición JPA |
|---|---|---|---|
| `company` | `description` | `text` (ilimitado) | `private String description` (sin `@Column(columnDefinition="TEXT")`) |

> **Riesgo:** JPA mapea `String` sin `@Column` a `varchar(255)` por defecto. Si algún registro tiene una descripción mayor a 255 caracteres, la escritura vía JPA fallará. Se recomienda agregar `@Column(columnDefinition = "TEXT")`.

---

## 6. Repositorio creado como parte de esta corrección

| Archivo | Motivo |
|---|---|
| `repositories/CompanyRepository.java` | La entidad `Company` no tenía repositorio JPA. Se creó para poder incluirla en el backup. |

---

## 7. Cambio técnico en `BackupService` — Transaccionalidad

Se agregó `@Transactional(readOnly = true)` al método `generarBackupExcel(...)` para garantizar que las relaciones `@ManyToOne` con carga `LAZY` (como `Egreso.proveedor`, `Proveedor.tipoEgreso`, `EstadisticaFin.tipoResultadoFin` y `CargueProductoConflicto.cargueProducto`) puedan ser accedidas sin error `LazyInitializationException` dentro de la misma sesión JPA.

---

## Resumen ejecutivo

| Categoría | Cantidad | Estado |
|---|---|---|
| Tablas faltantes en backup | 19 | ✅ Corregidas |
| Campos faltantes en tablas existentes | 1 (`precio_unidad`) | ✅ Corregido |
| Tabla BD sin entidad JPA | 1 (`tipo_conflicto`) | ⚠️ Pendiente (requiere decisión) |
| Entidades JPA sin tabla en schema public | 3 | ℹ️ Sin impacto (schema diferente o tabla eliminada) |
| Discrepancias tipo/longitud JPA vs BD | 4 | ⚠️ Requieren corrección en entidades |
| Repositorios nuevos creados | 1 (`CompanyRepository`) | ✅ Creado |
