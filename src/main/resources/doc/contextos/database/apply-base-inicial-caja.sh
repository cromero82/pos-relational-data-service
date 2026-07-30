#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"
echo ">>> 24_base_inicial_caja.sql"
psql "$DB" -v ON_ERROR_STOP=1 -f "$DIR/24_base_inicial_caja.sql"
echo "Base inicial caja: motivo INVERSION_INICIAL_BASE aplicado."
