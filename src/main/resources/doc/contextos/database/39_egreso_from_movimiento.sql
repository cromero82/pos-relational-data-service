-- 39 — Formalizar egreso desde movimiento «por identificar» (Para ordenar)
-- Idempotencia: un movimiento clearing solo puede formalizarse una vez.

ALTER TABLE egreso
    ADD COLUMN IF NOT EXISTS from_movimiento_origen_fondos_id BIGINT;

COMMENT ON COLUMN egreso.from_movimiento_origen_fondos_id IS
    'Movimiento OF (impacto +, p.ej. Para ordenar / MOVIMIENTO BANCO POR IDENTIFICAR) '
    'que se formaliza como egreso. La SALIDA_EGRESO sale de ese OF; no vuelve a restar el banco.';

CREATE UNIQUE INDEX IF NOT EXISTS uq_egreso_from_movimiento_origen_fondos
    ON egreso (from_movimiento_origen_fondos_id)
    WHERE from_movimiento_origen_fondos_id IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_egreso_from_movimiento_of'
    ) THEN
        ALTER TABLE egreso
            ADD CONSTRAINT fk_egreso_from_movimiento_of
            FOREIGN KEY (from_movimiento_origen_fondos_id)
            REFERENCES movimiento_origen_fondos (id)
            ON DELETE SET NULL;
    END IF;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE '39: skip FK from_movimiento: %', SQLERRM;
END $$;
