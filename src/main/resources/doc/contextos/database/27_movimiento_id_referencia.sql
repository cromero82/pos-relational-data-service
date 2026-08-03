-- Referencia polimórfica a la entidad de negocio que originó el movimiento.
-- Ej.: egreso.id (SALIDA_EGRESO), corte_venta.id (ENTRADA_VENTA / AJUSTE_CIERRE).
-- NULL en movimientos “naturales” (traslados, BASE_INICIAL, préstamos, entradas manuales).
-- Renombra origen_id (mismo significado) a id_referencia. Idempotente.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'movimiento_origen_fondos'
          AND column_name = 'origen_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'movimiento_origen_fondos'
          AND column_name = 'id_referencia'
    ) THEN
        ALTER TABLE movimiento_origen_fondos
            RENAME COLUMN origen_id TO id_referencia;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'movimiento_origen_fondos'
          AND column_name = 'id_referencia'
    ) THEN
        ALTER TABLE movimiento_origen_fondos
            ADD COLUMN id_referencia BIGINT;
    END IF;
END $$;

COMMENT ON COLUMN movimiento_origen_fondos.id_referencia IS
    'Id de la entidad de negocio asociada (egreso, corte_venta, …). NULL si no hay referencia externa. Usar junto con origen_tipo.';

CREATE INDEX IF NOT EXISTS idx_movimiento_origen_fondos_tipo_ref
    ON movimiento_origen_fondos (origen_tipo, id_referencia);
