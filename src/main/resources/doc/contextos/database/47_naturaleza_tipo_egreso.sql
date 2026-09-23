-- Catálogo de naturalezas (1) → tipos de egreso (N).
-- Sustituye la heurística inferNaturalezaFromTipoNombre.

CREATE TABLE IF NOT EXISTS naturaleza_tipo_egreso (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(40) NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    descripcion     TEXT,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_naturaleza_tipo_egreso_codigo UNIQUE (codigo)
);

COMMENT ON TABLE naturaleza_tipo_egreso IS
    'Naturaleza del gasto (catálogo). Un registro → muchos tipo_egreso.';
COMMENT ON COLUMN naturaleza_tipo_egreso.codigo IS
    'Código estable alineado a egreso.naturaleza (COMPRA_MERCANCIA, PERSONAL, …).';

INSERT INTO naturaleza_tipo_egreso (codigo, nombre, descripcion) VALUES
    ('COMPRA_MERCANCIA', 'Compra mercancía', 'Compra de producto / mercancía para reventa'),
    ('GASTO_OPERATIVO', 'Gasto operativo', 'Servicios, mantenimiento, adecuaciones, etc.'),
    ('PERSONAL', 'Personal / nómina / anticipo', 'Pagos a personal (sin liquidar nómina en el POS)'),
    ('DIVIDENDOS', 'Dividendos / distribución', 'Pago de utilidades / dividendos'),
    ('TRIBUTO', 'Tributo / impuestos', 'Impuestos y obligaciones tributarias'),
    ('OTRO', 'Otro', 'Otros egresos no clasificados')
ON CONFLICT (codigo) DO NOTHING;

ALTER TABLE tipo_egreso
    ADD COLUMN IF NOT EXISTS naturaleza_tipo_egreso_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_tipo_egreso_naturaleza'
    ) THEN
        ALTER TABLE tipo_egreso
            ADD CONSTRAINT fk_tipo_egreso_naturaleza
            FOREIGN KEY (naturaleza_tipo_egreso_id)
            REFERENCES naturaleza_tipo_egreso (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_tipo_egreso_naturaleza
    ON tipo_egreso (naturaleza_tipo_egreso_id);

-- Backfill tipos (misma heurística que el SQL 46, una sola vez)
UPDATE tipo_egreso t
SET naturaleza_tipo_egreso_id = n.id
FROM naturaleza_tipo_egreso n
WHERE t.naturaleza_tipo_egreso_id IS NULL
  AND n.codigo = CASE
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
      END;

-- Tipos sin match → OTRO
UPDATE tipo_egreso t
SET naturaleza_tipo_egreso_id = n.id
FROM naturaleza_tipo_egreso n
WHERE t.naturaleza_tipo_egreso_id IS NULL
  AND n.codigo = 'OTRO';

-- Alinear egreso.naturaleza al código del catálogo vía tipo
UPDATE egreso e
SET naturaleza = n.codigo
FROM tipo_egreso t
JOIN naturaleza_tipo_egreso n ON n.id = t.naturaleza_tipo_egreso_id
WHERE e.tipo_egreso_id = t.id
  AND (e.naturaleza IS NULL OR btrim(e.naturaleza::text) = '' OR e.naturaleza::text <> n.codigo);

COMMENT ON COLUMN tipo_egreso.naturaleza_tipo_egreso_id IS
    'Naturaleza sugerida al elegir este tipo en un egreso (catálogo).';
