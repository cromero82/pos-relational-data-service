#!/usr/bin/env bash
# Aplica 28_origen_fondos_estado_archivar.sql
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/28_origen_fondos_estado_archivar.sql"
echo ">>> 28_origen_fondos_estado_archivar aplicado."
