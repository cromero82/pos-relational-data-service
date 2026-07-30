#!/usr/bin/env bash
# Hotfix — check constraint TRASLADO (pata entrada)
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> 21_fix_traslado_destino_check.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/21_fix_traslado_destino_check.sql"
echo "Hotfix traslado destino check aplicado."
