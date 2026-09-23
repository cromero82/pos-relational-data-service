#!/usr/bin/env bash
# Sprint 4 — finanzas / arqueo / permisos
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

for f in \
  07_egreso_metodo_pago.sql \
  08_corte_venta_extend.sql \
  06_funcionalidad_pos.sql \
  09_metodo_pago_extend.sql
do
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done
echo "Sprint 4 aplicado."
