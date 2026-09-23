#!/usr/bin/env bash
# Migración canónica PC de tienda → controlneg_rmx_db_v02.
# Crea la BD si falta (pg_dump de controlneg_rmx_db + pg_restore --no-acl).
# Nunca --create. Nunca pisa controlneg_rmx_db.
#
#   ./apply-migrate-tienda-infinito-v02.sh
#   DB_URL=postgresql://romax-admin:PASS@localhost:5432/controlneg_rmx_db_v02 \
#     SRC_DB=controlneg_rmx_db ./apply-migrate-tienda-infinito-v02.sh
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DB="${SRC_DB:-controlneg_rmx_db}"
DEST_DB="${DEST_DB:-controlneg_rmx_db_v02}"
PGUSER="${PGUSER:-romax-admin}"
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
DB="${1:-postgresql://${PGUSER}:${PGPASSWORD:-f4ast3rv3rs10n*}@${PGHOST}:${PGPORT}/${DEST_DB}}"

if [[ "$DB" == *"/controlneg_rmx_db" && "$DB" != *"/controlneg_rmx_db_v02"* ]]; then
  echo "ERROR: este script solo corre sobre ${DEST_DB}, no sobre ${SRC_DB}." >&2
  exit 1
fi

echo ">>> Tienda Infinito v02"
echo "    destino: ${DEST_DB}  origen copia: ${SRC_DB}"

exists="$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d postgres -Atqc \
  "SELECT 1 FROM pg_database WHERE datname='${DEST_DB}'" || true)"
if [[ "$exists" != "1" ]]; then
  echo ">>> ${DEST_DB} no existe: dump ${SRC_DB} → restore (sin --create)"
  DUMP="${TMPDIR:-/tmp}/controlneg_rmx_db-$(date +%Y%m%d%H%M).dump"
  pg_dump -Fc -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$SRC_DB" -f "$DUMP"
  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d postgres -v ON_ERROR_STOP=1 \
    -c "CREATE DATABASE ${DEST_DB} OWNER \"${PGUSER}\";"
  pg_restore --no-owner --no-acl -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$DEST_DB" "$DUMP"
  echo "    dump: $DUMP"
else
  echo ">>> ${DEST_DB} ya existe; solo scripts (idempotentes)"
fi

while IFS= read -r line || [[ -n "$line" ]]; do
  f="${line%%#*}"
  f="$(echo "$f" | tr -d '[:space:]')"
  [[ -z "$f" ]] && continue
  echo ">>> $f"
  psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
done < "$DIR/migrate-tienda-infinito-v02.files"

echo ">>> Migración Tienda Infinito v02 lista (sin 25_repair, sin 61_)."
