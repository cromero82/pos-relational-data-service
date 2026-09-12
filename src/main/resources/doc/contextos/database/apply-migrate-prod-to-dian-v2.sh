#!/usr/bin/env bash
# Migración schema prod → dian-v2 (orígenes de fondos, ledger, cierres, distribución, base inicial).
# Idempotente en lo posible. Omite 25_repair_* (solo pruebas).
# Uso:
#   ./apply-migrate-prod-to-dian-v2.sh
#   ./apply-migrate-prod-to-dian-v2.sh "$DB_URL"
#
# Dump prod viejo (sin inventario / notif / HRE): ANTES de este wrapper, o el script
# fallará en 10_ / 36_ / 44_:
#   entrada_inventario.sql
#   historial_precio_producto.sql
#   28_confirmacion_pagos_electronicos.sql
#   29_notificacion_email_archivada.sql
#   30_plantilla_notificacion_pago.sql
#   31_ticket_sin_notificacion.sql
#   33_plantilla_notificacion_movimiento.sql
# Tras 28_confirmacion, email_alerta_pagos de establecimiento queda en pagos@…;
# en pila tienda-infinito cambiar a tienda-infinito@mayaksoluciones.com.
# Historial Tickets: 32_ (HRE.nombre_cliente) y 62_ (VTA- no VTA-LEGACY).
# OF internos: 63_ (Caja Menor / Caja General; 14_ deja labels de metodo_pago prod).
# Apertura efectivo: 64_ (Contado legacy → base config + resto Caja Menor).
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
  62_normalize_documento_venta_vta.sql \
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
  63_align_caja_menor_general.sql \
  27_movimiento_id_referencia.sql \
  28_origen_fondos_estado_archivar.sql \
  30_historial_recibo_pago.sql \
  34_ticket_rapido_origen_y_backfill.sql \
  35_desfase_motivos_accion.sql \
  36_notificacion_legalizar.sql \
  37_cxc_abonos_schema.sql \
  38_duenos_clasificacion_movimiento.sql \
  39_egreso_from_movimiento.sql \
  40_rename_para_ordenar_sin_clasificar.sql \
  41_cxc_abrir_desde_ticket.sql \
  42_cxc_total_ticket_sync.sql \
  43_cxc_anular_castigar.sql \
  44_hre_abono_cxc.sql \
  45_hre_monto_recibido.sql \
  32_historial_recibos_electronicos_nombre_cliente.sql \
  64_migrate_distribucion_contado_legacy.sql \
  65_corte_venta_total_ventas_sistema.sql
do
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done

echo ">>> Migración prod → dian-v2 aplicada (sin 25_repair)."
