#!/usr/bin/env bash
# Migración schema prod → dian-v2 (orígenes de fondos, ledger, cierres, distribución, base inicial).
# Idempotente en lo posible. Omite 25_repair_* (solo pruebas).
# Uso:
#   ./apply-migrate-prod-to-dian-v2.sh
#   ./apply-migrate-prod-to-dian-v2.sh "$DB_URL"
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> Migración prod → dian-v2"
echo "    DB: ${DB%%@*}@***"

for f in \
  00_establecimiento.sql \
  01_consecutivo_documento.sql \
  02_motivo_operacion.sql \
  03_tipo_movimiento_inventario.sql \
  04_alinear_estado_recibos.sql \
  05_documento_venta.sql \
  05_backfill_documento_venta.sql \
  07_egreso_metodo_pago.sql \
  08_corte_venta_extend.sql \
  06_funcionalidad_pos.sql \
  09_metodo_pago_extend.sql \
  10_movimiento_inventario.sql \
  11_inventario_kardex.sql \
  12_establecimiento_manejo_cuentas.sql \
  13_bolsillos_catalogos.sql \
  14_cuenta_bolsillo.sql \
  15_movimiento_bolsillo.sql \
  16_egreso_cuenta_bolsillo.sql \
  17_cuenta_bolsillo_parent.sql \
  18_rename_origen_fondos.sql \
  19_cierre_motivo_desfase.sql \
  20_corte_venta_workflow.sql \
  21_fix_traslado_destino_check.sql \
  22_corte_venta_ultimo_movimiento.sql \
  23_distribucion_efectivo.sql \
  24_base_inicial_caja.sql \
  26_unlink_caja_menor_metodo_pago.sql \
  27_movimiento_id_referencia.sql \
  28_origen_fondos_estado_archivar.sql
do
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done

echo ">>> Migración prod → dian-v2 aplicada (sin 25_repair)."
