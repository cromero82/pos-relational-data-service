-- Sprint B1 — Modo estricto/flexible de cuentas en establecimiento

ALTER TABLE establecimiento
    ADD COLUMN IF NOT EXISTS manejo_estricto_cuentas BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE establecimiento
SET manejo_estricto_cuentas = FALSE
WHERE manejo_estricto_cuentas IS NULL;
