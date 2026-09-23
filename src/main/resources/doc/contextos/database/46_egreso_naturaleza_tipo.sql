-- Plan 1 egresos: tipo snapshot + naturaleza en el documento.
-- Cuenta del dueño (OF) sigue siendo clasificación; bajar saldo = egreso de pago.

ALTER TABLE egreso
    ADD COLUMN IF NOT EXISTS tipo_egreso_id BIGINT,
    ADD COLUMN IF NOT EXISTS naturaleza VARCHAR(40);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_egreso_tipo_egreso'
    ) THEN
        ALTER TABLE egreso
            ADD CONSTRAINT fk_egreso_tipo_egreso
            FOREIGN KEY (tipo_egreso_id) REFERENCES tipo_egreso (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_egreso_tipo_egreso_id ON egreso (tipo_egreso_id);
CREATE INDEX IF NOT EXISTS idx_egreso_naturaleza ON egreso (naturaleza);

-- Backfill tipo desde proveedor
UPDATE egreso e
SET tipo_egreso_id = p.tipo_egreso_id
FROM proveedor p
WHERE e.proveedor_id = p.id
  AND e.tipo_egreso_id IS NULL
  AND p.tipo_egreso_id IS NOT NULL;

-- Backfill naturaleza desde nombre de tipo (misma heurística que el BE)
UPDATE egreso e
SET naturaleza = CASE
    WHEN LOWER(t.nombre) LIKE '%compra%' AND LOWER(t.nombre) LIKE '%proveedor%' THEN 'COMPRA_MERCANCIA'
    WHEN LOWER(t.nombre) LIKE '%personal%' OR LOWER(t.nombre) LIKE '%nomina%' OR LOWER(t.nombre) LIKE '%nómina%' THEN 'PERSONAL'
    WHEN LOWER(t.nombre) LIKE '%dividendo%' THEN 'DIVIDENDOS'
    WHEN LOWER(t.nombre) LIKE '%impuesto%' OR LOWER(t.nombre) LIKE '%tribut%' THEN 'TRIBUTO'
    WHEN LOWER(t.nombre) LIKE '%operativ%'
      OR LOWER(t.nombre) LIKE '%servicio%'
      OR LOWER(t.nombre) LIKE '%manten%'
      OR LOWER(t.nombre) LIKE '%adecu%'
      OR LOWER(t.nombre) LIKE '%reposic%'
      OR LOWER(t.nombre) LIKE '%public%' THEN 'GASTO_OPERATIVO'
    ELSE 'OTRO'
END
FROM tipo_egreso t
WHERE e.tipo_egreso_id = t.id
  AND (e.naturaleza IS NULL OR btrim(e.naturaleza) = '');

COMMENT ON COLUMN egreso.tipo_egreso_id IS
    'Snapshot del tipo al momento del egreso (no depende de cambios futuros del proveedor).';
COMMENT ON COLUMN egreso.naturaleza IS
    'COMPRA_MERCANCIA|GASTO_OPERATIVO|PERSONAL|DIVIDENDOS|TRIBUTO|OTRO. Sin RETIRO_DUENO (OF Cuenta del dueño).';
