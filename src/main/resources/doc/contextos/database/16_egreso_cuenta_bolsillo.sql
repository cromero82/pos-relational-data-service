-- Sprint B4 — Egreso vinculado a bolsillo origen

ALTER TABLE egreso
    ADD COLUMN IF NOT EXISTS cuenta_bolsillo_id INTEGER NULL REFERENCES cuenta_bolsillo (id);

CREATE INDEX IF NOT EXISTS idx_egreso_cuenta_bolsillo ON egreso (cuenta_bolsillo_id);

-- Backfill: egresos existentes → cuenta del mismo metodo_pago
UPDATE egreso e
SET cuenta_bolsillo_id = cb.id
FROM cuenta_bolsillo cb
WHERE e.cuenta_bolsillo_id IS NULL
  AND e.metodo_pago_id IS NOT NULL
  AND cb.metodo_pago_id = e.metodo_pago_id;
