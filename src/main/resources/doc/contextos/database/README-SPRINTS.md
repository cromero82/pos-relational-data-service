# Scripts SQL — orden de ejecución (POS Plan Maestro)

Base de datos: `controlneg_rmx_db` (schema `public`).

## Sprint 0 — Catálogos y capa documental (obligatorio antes de Sprint 1 en runtime)

Ejecutar **en orden**:

```bash
psql -U postgres -d controlneg_rmx_db -f 00_establecimiento.sql
psql -U postgres -d controlneg_rmx_db -f 01_consecutivo_documento.sql
psql -U postgres -d controlneg_rmx_db -f 02_motivo_operacion.sql
psql -U postgres -d controlneg_rmx_db -f 03_tipo_movimiento_inventario.sql
psql -U postgres -d controlneg_rmx_db -f 04_alinear_estado_recibos.sql
psql -U postgres -d controlneg_rmx_db -f 05_documento_venta.sql
psql -U postgres -d controlneg_rmx_db -f 05_backfill_documento_venta.sql   # una vez, ventas históricas
```

O usar: `./apply-sprint0.sh`

## Ya implementados (no repetir)

- `entrada_inventario.sql`
- `historial_precio_producto.sql`

## Sprint 3 — Kardex (movimiento + libro auxiliar)

```bash
./apply-sprint3.sh
# o todo junto:
./apply-all-sprints.sh
```

Scripts:
- `10_movimiento_inventario.sql`
- `11_inventario_kardex.sql`

## Sprint 4 — Finanzas, arqueo y permisos

```bash
./apply-sprint4.sh
# o todo junto:
./apply-all-sprints.sh
```

Scripts:
- `07_egreso_metodo_pago.sql`
- `08_corte_venta_extend.sql`
- `06_funcionalidad_pos.sql`
- `09_metodo_pago_extend.sql` — `descripcion_egreso`, `visible_pago_tickets`, `monto`; registro id=4 base proveedores

## Sprint B1–B2 — Orígenes de fondos (ex bolsillos)

```bash
./apply-bolsillos-p1.sh
```

Scripts (nombres históricos de archivo; tablas actuales renombradas en `18`):
- `12_establecimiento_manejo_cuentas.sql` — `manejo_estricto_cuentas`
- `13_bolsillos_catalogos.sql` — crea `tipo_bolsillo` → hoy `tipo_origen_fondos`
- `14_cuenta_bolsillo.sql` — crea `cuenta_bolsillo` → hoy `origen_fondos`
- `15_movimiento_bolsillo.sql` — crea `movimiento_bolsillo` → hoy `movimiento_origen_fondos`

## Sprint B4 — Egreso ↔ origen

```bash
./apply-bolsillos-p2.sh
```

Scripts:
- `16_egreso_cuenta_bolsillo.sql` — `egreso.cuenta_bolsillo_id` → hoy `origen_fondos_id`

## Jerarquía + renombrado dominio

```bash
./apply-bolsillos-p3.sh          # 17 parent_id
./apply-origen-fondos-rename.sh  # 18 rename → origen_fondos
```

Scripts:
- `17_cuenta_bolsillo_parent.sql` — `parent_origen_fondos_id` (tras rename)
- `18_rename_origen_fondos.sql` — renombra tablas/columnas a `origen_fondos`

## Sprint B5 — Cierre + motivo desfase

```bash
./apply-bolsillos-b5.sh
```

Scripts:
- `19_cierre_motivo_desfase.sql` — `ventas_tipo.motivo_desfase_id` + seeds categoría `DESFASE_CIERRE`

Ver `infinito-ai-front/.cursor/rules/movimientos-almacen/bolsillos-planificacion.md` §17.4.

## Sprint B8 — Workflow de cierre y revisión

```bash
./apply-bolsillos-b8.sh
```

Scripts:
- `20_corte_venta_workflow.sql` — estado/observación en cabecera,
  `corte_venta_detalle`, backfill desde `ventas_tipo` e índices.

Los históricos migrados quedan `revisada`; los nuevos cierres de cajero nacen
`creada` y los creados por admin nacen `revisada`.

## Post-B8 — Watermarks, distribución, base inicial, id_referencia (2026-07/08)

```bash
# Migración completa prod → dian-v2 (idempotente en lo posible; omite 25_repair)
./apply-migrate-prod-to-dian-v2.sh
# o con URL: ./apply-migrate-prod-to-dian-v2.sh "$DB_URL"
```

Scripts (después de `20`):

| SQL | Efecto | Apply helper |
|-----|--------|----------------|
| `21_fix_traslado_destino_check.sql` | CHECK destino en traslados | `apply-fix-traslado-destino.sh` |
| `22_corte_venta_ultimo_movimiento.sql` | `ultimo_movimiento_origen_fondos_id` | `apply-corte-watermark-movimientos.sh` |
| `23_distribucion_efectivo.sql` | `base_siguiente_efectivo` + distribución | `apply-distribucion-efectivo.sh` |
| `24_base_inicial_caja.sql` | Motivo/base inicial instalación | `apply-base-inicial-caja.sh` |
| `25_repair_entrada_venta_corte1.sql` | Repair puntual de prueba — **no** en migrate prod | — |
| `26_unlink_caja_menor_metodo_pago.sql` | Caja Menor sin `metodo_pago_id` | `apply-unlink-caja-menor-mp.sh` |
| `27_movimiento_id_referencia.sql` | `origen_id` → `id_referencia` | (incluido en migrate) |
| `28_origen_fondos_estado_archivar.sql` | `estado` ACTIVO/ARCHIVADO + unique nombre hermanos | `apply-origen-fondos-estado-archivar.sh` |
| `30_historial_recibo_pago.sql` | Multipago: `historial_recibo_pago` + `metodo_pago.codigo_dian_payment_means` + backfill 1:1 | (incluido en migrate) |
| `34_ticket_rapido_origen_y_backfill.sql` | Flag `ticket_rapido` + backfill pagos | (incluido en migrate) |
| `35_desfase_motivos_accion.sql` | `accion_esperada` + `MOVIMIENTO_NO_REGISTRADO` | (incluido en migrate) |
| `36_notificacion_legalizar.sql` | Clasificación notif. email + motivos LEGALIZAR_* | (incluido en migrate) |
| `37_cxc_abonos_schema.sql` | `cuenta_por_cobrar` + `abono_cxc` (UI luego) | (incluido en migrate) |
| `38_duenos_clasificacion_movimiento.sql` | Raíz **Dueños** + `Cuenta del dueño`; `clasificacion_operativa` / `periodo_cierre_id` en ledger | (incluido en migrate) |
| `39_egreso_from_movimiento.sql` | `egreso.from_movimiento_origen_fondos_id` (Formalizar egreso sin doble resta banco) | (incluido en migrate) |
| `40_rename_para_ordenar_sin_clasificar.sql` | OF «Para ordenar» → **Sin Clasificar** | (incluido en migrate) |
| `42_cxc_total_ticket_sync.sql` | CxC `total_ticket` + sync al mutar ítems | (incluido en migrate) |
| `43_cxc_anular_castigar.sql` | CxC `CASTIGADA` + traza cierre + tipo `CASTIGO_CARTERA` | (incluido en migrate) |
| `44_hre_abono_cxc.sql` | HRE: `abono_cxc_id` + XOR con `historial_recibo_id` (panel QR abonos) | (incluido en migrate) |
| `45_hre_monto_recibido.sql` | HRE: `monto_recibido` (match QR con monto ≠ esperado) | (incluido en migrate) |
| `46_`…`49_` | Egresos: naturaleza/tipo, persona, `es_dueno_propietario` | (manual / apply egresos) |
| `50_` / `51_` | Presentaciones UoM producto | (manual) |
| `52_abono_cxc_cliente_pagador.sql` | Pagador del abono CxC | (manual) |
| `53_` / `54_` | `permite_notificacion` + plantilla naturaleza Ingreso | (manual) |
| `55_notificaciones_activa.sql` | `configuracion_app.notificaciones.activa` (ocultar panel UI) | (manual, idempotente) |
| `56_ticket_observaciones.sql` | `ticket.observaciones` TEXT | (manual, idempotente) |
| `58_asociaciones_egresos_obligatorio.sql` | `configuracion_app` key `notificaciones.asociaciones-egresos.obligatorio` | (manual, idempotente) |
| `60_estadistica_fin_total_cobranzas.sql` | `estadistica_fin.total_cobranzas` (abonos CxC en Resumen) | (manual, idempotente) |

### QA — desde la última oleada que trajo SQL (`53`→`54`, 2026-09-02)

La oleada Historial Tickets (2026-09-03) **no** trajo SQL. Correr **en este orden** en `controlneg_rmx_db` y/o `controlneg_rmx_db_sandbox`:

```bash
psql -U romax-admin -d controlneg_rmx_db -f 55_notificaciones_activa.sql
psql -U romax-admin -d controlneg_rmx_db -f 56_ticket_observaciones.sql
psql -U romax-admin -d controlneg_rmx_db -f 57_egreso_origen_fondos.sql
psql -U romax-admin -d controlneg_rmx_db -f 58_asociaciones_egresos_obligatorio.sql
```

No hay SQL para el asistente de cierre de caja ni para minimizar el panel (UI / `localStorage`).

**Formalizar egreso (smoke):** Orígenes → Sin Clasificar → fila `MOVIMIENTO BANCO POR IDENTIFICAR` → «Formalizar egreso» → proveedor → POST con `fromMovimientoOrigenFondosId`. Esperado: egreso + `SALIDA_EGRESO` solo en bolsa; banco sin 2ª resta; reintento → error idempotente.

**Núcleo ingresos:** dashboard = Ventas sistema. Glosario: `prompts-general-pos/GLOSARIO-NUCLEO-FINANCIERO.md`. Canónico de corte: `corte_venta_detalle`.

> Nota: `28_confirmacion_pagos_electronicos.sql` / `29_notificacion_email_archivada.sql` son de pagos QR/email; no forman parte del wrapper OF. El multipago es **`30_`**.

Contexto de dominio: `../AI-HANDOFF-FINANZAS-2026-08.md`.

## Reset operativo de prueba (no es migración de schema)

Vacía transacciones (movimientos OF, egresos, cortes, tickets, stats…) y **conserva catálogos**.

```bash
# Canónico v2 — ver instructivo (DBeaver Auto-commit ON)
bash /Users/carlosromero/Documents/dev/repos/prompts-general-pos/apply-reset-tablas-financieras-transaccionales.sh
# o:
psql "$DB_URL" -v ON_ERROR_STOP=1 \
  -f /Users/carlosromero/Documents/dev/repos/prompts-general-pos/reset-tablas-financieras-transaccionales-v2.sql
```

Docs: `prompts-general-pos/RESET-TABLAS-FINANCIERAS-TRANSACCIONALES.md`.  
Tras reset: logout + login **admin** → modal base inicial (si `BASE_INICIAL=0` y sin cortes).

## Sprint 5+ (pendiente en repo)
- `12_backup_registro.sql`

Ver `infinito-ai-front/.cursor/rules/movimientos-almacen/POS-PLAN-MAESTRO.md` §7.
