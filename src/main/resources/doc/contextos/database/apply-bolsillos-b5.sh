#!/usr/bin/env bash
# Sprint B5 — motivo desfase en cierre
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> 19_cierre_motivo_desfase.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/19_cierre_motivo_desfase.sql"
echo "Sprint B5 motivo desfase aplicado."
