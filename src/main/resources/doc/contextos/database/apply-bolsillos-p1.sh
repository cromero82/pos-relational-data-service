#!/usr/bin/env bash
# Sprint B1 — bolsillos / cuentas internas
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

for f in \
  12_establecimiento_manejo_cuentas.sql \
  13_bolsillos_catalogos.sql \
  14_cuenta_bolsillo.sql \
  15_movimiento_bolsillo.sql
do
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done
echo "Sprint B1 bolsillos aplicado."
