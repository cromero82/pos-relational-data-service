#!/usr/bin/env bash
# Aplica Sprint 0 + Sprint 3 en orden
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB="${1:-postgresql://romax-admin:f4ast3rv3rs10n*@localhost:5432/controlneg_rmx_db}"

bash "$DIR/apply-sprint0.sh" "$DB"
bash "$DIR/apply-sprint3.sh" "$DB"
bash "$DIR/apply-sprint4.sh" "$DB"
echo "Sprints 0, 3 y 4 aplicados."
