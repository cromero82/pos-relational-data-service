-- 28 — Estado ARCHIVADO en origen_fondos + unicidad de nombre entre hermanos activos
-- Idempotente.

ALTER TABLE origen_fondos
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20);

UPDATE origen_fondos
SET estado = CASE WHEN COALESCE(activo, TRUE) THEN 'ACTIVO' ELSE 'ARCHIVADO' END
WHERE estado IS NULL;

ALTER TABLE origen_fondos
    ALTER COLUMN estado SET DEFAULT 'ACTIVO';

ALTER TABLE origen_fondos
    ALTER COLUMN estado SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_origen_fondos_estado'
    ) THEN
        ALTER TABLE origen_fondos
            ADD CONSTRAINT ck_origen_fondos_estado
            CHECK (estado IN ('ACTIVO', 'ARCHIVADO'));
    END IF;
END $$;

COMMENT ON COLUMN origen_fondos.estado IS
    'ACTIVO | ARCHIVADO. Archivar exige saldo ledger = 0; se sincroniza con activo=false.';

-- Nombre único entre hermanos activos (case-insensitive, trim).
CREATE UNIQUE INDEX IF NOT EXISTS uq_origen_fondos_hermano_nombre_activo
    ON origen_fondos (parent_origen_fondos_id, lower(btrim(nombre)))
    WHERE parent_origen_fondos_id IS NOT NULL
      AND activo = TRUE
      AND estado = 'ACTIVO';
