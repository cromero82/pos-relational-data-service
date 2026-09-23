#!/usr/bin/env bash
# Jerarquía parent_id en cuentas bolsillo
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

echo ">>> 17_cuenta_bolsillo_parent.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/17_cuenta_bolsillo_parent.sql"
echo "Jerarquía bolsillos aplicada."
