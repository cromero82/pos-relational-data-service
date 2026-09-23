#!/usr/bin/env bash
# Aplica Sprint 0 en controlneg_rmx_db. Uso: ./apply-sprint0.sh [connection_uri]
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

for f in \
  00_establecimiento.sql \
  01_consecutivo_documento.sql \
  02_motivo_operacion.sql \
  03_tipo_movimiento_inventario.sql \
  04_alinear_estado_recibos.sql \
  05_documento_venta.sql \
  05_backfill_documento_venta.sql
do
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done
echo "Sprint 0 aplicado."
