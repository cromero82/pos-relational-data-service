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
