#!/usr/bin/env bash
# Renombra tablas bolsillo → origen_fondos
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> 18_rename_origen_fondos.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/18_rename_origen_fondos.sql"
echo "Renombrado origen_fondos aplicado."
