#!/usr/bin/env bash
# Sprint 3 — kardex. Ejecutar después de apply-sprint0.sh
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

for f in \
  10_movimiento_inventario.sql \
  11_inventario_kardex.sql
do
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done
echo "Sprint 3 (kardex) aplicado."
