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

## Sprint 5+ (pendiente en repo)
- `12_backup_registro.sql`

Ver `infinito-ai-front/.cursor/rules/movimientos-almacen/POS-PLAN-MAESTRO.md` §7.
