#!/usr/bin/env bash
# Watermark movimientos en corte_venta
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> 22_corte_venta_ultimo_movimiento.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/22_corte_venta_ultimo_movimiento.sql"
echo "Watermark ultimo_movimiento_origen_fondos_id aplicado."
