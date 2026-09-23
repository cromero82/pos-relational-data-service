-- 42 — CxC: total del ticket de referencia para sincronizar saldo al agregar/quitar ítems

ALTER TABLE cuenta_por_cobrar
    ADD COLUMN IF NOT EXISTS total_ticket NUMERIC(12, 2);

COMMENT ON COLUMN cuenta_por_cobrar.total_ticket IS
    'Total del ticket (suma productos) la última vez que se sincronizó la CxC. '
    'Al crecer/decrecer el ticket, saldo y monto_original se ajustan por el delta.';

-- Backfill: recibo.total si hay vínculo; si no, monto_original
UPDATE cuenta_por_cobrar c
SET total_ticket = COALESCE(
    NULLIF(r.total, 0),
    c.monto_original
)
FROM recibo r
WHERE c.recibo_id = r.id
  AND c.total_ticket IS NULL;

UPDATE cuenta_por_cobrar
SET total_ticket = monto_original
WHERE total_ticket IS NULL;
