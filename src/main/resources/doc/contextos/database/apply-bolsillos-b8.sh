#!/usr/bin/env bash
# Sprint B8 — workflow cierre + detalle + revisión
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> 20_corte_venta_workflow.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/20_corte_venta_workflow.sql"
echo "Sprint B8 workflow de cierre aplicado."
