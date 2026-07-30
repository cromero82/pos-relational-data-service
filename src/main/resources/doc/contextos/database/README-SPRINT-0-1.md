# Scripts BD — Sprint 0 y 1 (POS plan maestro)

Ejecutar en **orden** sobre `controlneg_rmx_db`:

```bash
cd pos-relational-data-service/src/main/resources/doc/contextos/database

psql -h localhost -U romax-admin -d controlneg_rmx_db -f 00_establecimiento.sql
psql -h localhost -U romax-admin -d controlneg_rmx_db -f 01_consecutivo_documento.sql
psql -h localhost -U romax-admin -d controlneg_rmx_db -f 02_motivo_operacion.sql
psql -h localhost -U romax-admin -d controlneg_rmx_db -f 05_documento_venta.sql
psql -h localhost -U romax-admin -d controlneg_rmx_db -f 05_backfill_documento_venta.sql
```

Scripts **05+** (egreso, corte, kardex, backup) se añaden en sprints posteriores.

Ver [`infinito-ai-front/.cursor/rules/movimientos-almacen/POS-PLAN-MAESTRO.md`](../../../../../../infinito-ai-front/.cursor/rules/movimientos-almacen/POS-PLAN-MAESTRO.md).
