#!/usr/bin/env bash
# Sprint B4 — egreso ↔ bolsillo
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> 16_egreso_cuenta_bolsillo.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/16_egreso_cuenta_bolsillo.sql"
echo "Sprint B4 egreso-bolsillo aplicado."
