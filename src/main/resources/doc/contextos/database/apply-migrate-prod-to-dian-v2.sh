#!/usr/bin/env bash
# Aplica el manifiesto migrate-tienda-infinito-v02.files.
# Destino MUST ser controlneg_rmx_db_v02. Nunca controlneg_rmx_db (dev/prod viva).
# Omite 25_repair_* y 61_sync_catalogos (solo laptop).
#
# Uso:
#   ./apply-migrate-prod-to-dian-v2.sh 'postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db_v02'
#
# En la PC de tienda preferir Infinito Launcher → Tienda Infinito → Ejecutar scripts.
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MANIFEST="$DIR/migrate-tienda-infinito-v02.files"
DB="${1:-}"

if [[ -z "$DB" ]]; then
  echo "Uso: $0 'postgresql://USER:PASS@localhost:5432/controlneg_rmx_db_v02'"
  echo "    Destino obligatorio: controlneg_rmx_db_v02"
  exit 1
fi
if [[ "$DB" == *controlneg_rmx_db* && "$DB" != *controlneg_rmx_db_v02* ]]; then
  echo "RECHAZADO: no aplicar sobre controlneg_rmx_db (sin _v02)."
  exit 1
fi
if [[ ! -f "$MANIFEST" ]]; then
  echo "Falta manifiesto: $MANIFEST"
  exit 1
fi

echo ">>> Migración prod → dian-v2 (manifiesto)"
echo "    DB: ${DB%%@*}@***"

while IFS= read -r raw || [[ -n "$raw" ]]; do
  f="${raw%%#*}"
  f="${f#"${f%%[![:space:]]*}"}"
  f="${f%"${f##*[![:space:]]}"}"
  [[ -z "$f" ]] && continue
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done < "$MANIFEST"

echo ">>> Migración aplicada (sin 25_repair, sin 61_sync)."
