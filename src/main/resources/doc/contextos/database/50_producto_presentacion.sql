-- =============================================================================
-- 50_producto_presentacion.sql
-- Presentaciones / UoM vendibles (paquete, unidad) + snapshots en líneas de venta.
-- Stock canónico sigue en producto.existencia (unidad base).
-- =============================================================================

-- Presentaciones vendibles por producto
CREATE TABLE IF NOT EXISTS producto_presentacion (
    id                BIGSERIAL PRIMARY KEY,
    producto_id       BIGINT         NOT NULL REFERENCES producto (id) ON DELETE CASCADE,
    codigo            VARCHAR(32)    NOT NULL,
    nombre_mostrar    VARCHAR(120)   NOT NULL,
    factor_a_base     NUMERIC(12, 4) NOT NULL DEFAULT 1,
    precio_venta      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    codigo_barras_alt VARCHAR(64),
    es_default_venta  BOOLEAN        NOT NULL DEFAULT FALSE,
    activo            BOOLEAN        NOT NULL DEFAULT TRUE,
    fecha_creacion    TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT uq_producto_presentacion_codigo UNIQUE (producto_id, codigo),
    CONSTRAINT ck_presentacion_factor_pos CHECK (factor_a_base > 0)
);

CREATE INDEX IF NOT EXISTS idx_pp_producto_id ON producto_presentacion (producto_id);
CREATE INDEX IF NOT EXISTS idx_pp_barcode_alt ON producto_presentacion (codigo_barras_alt)
    WHERE codigo_barras_alt IS NOT NULL;

COMMENT ON TABLE producto_presentacion IS
    'UoM vendible del producto (PAQUETE, UNIDAD, …). Stock vive en producto.existencia (base).';
COMMENT ON COLUMN producto_presentacion.factor_a_base IS
    'Cuántas unidades base representa 1 cantidad de esta presentación (paquete x20 → 20).';

-- Líneas de ticket abiertas
ALTER TABLE recibo_detalle
    ADD COLUMN IF NOT EXISTS presentacion_id BIGINT REFERENCES producto_presentacion (id),
    ADD COLUMN IF NOT EXISTS precio_unitario_snapshot NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS factor_snapshot NUMERIC(12, 4),
    ADD COLUMN IF NOT EXISTS cantidad_base NUMERIC(14, 4);

CREATE INDEX IF NOT EXISTS idx_rd_presentacion_id ON recibo_detalle (presentacion_id);

-- Líneas históricas (cierres)
ALTER TABLE historial_recibo_detalle
    ADD COLUMN IF NOT EXISTS presentacion_id BIGINT REFERENCES producto_presentacion (id),
    ADD COLUMN IF NOT EXISTS precio_unitario_snapshot NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS factor_snapshot NUMERIC(12, 4),
    ADD COLUMN IF NOT EXISTS cantidad_base NUMERIC(14, 4);

CREATE INDEX IF NOT EXISTS idx_hrd_presentacion_id ON historial_recibo_detalle (presentacion_id);

-- Edición de ticket
ALTER TABLE edicion_recibo_detalle
    ADD COLUMN IF NOT EXISTS presentacion_id BIGINT REFERENCES producto_presentacion (id),
    ADD COLUMN IF NOT EXISTS precio_unitario_snapshot NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS factor_snapshot NUMERIC(12, 4),
    ADD COLUMN IF NOT EXISTS cantidad_base NUMERIC(14, 4);

-- Opcional: marca unidad base en producto (documental)
ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS unidad_base_codigo VARCHAR(32) DEFAULT 'UNIDAD';

-- Historial de precio por presentación (sprint 2; solo si existe la tabla)
DO $$
BEGIN
  IF EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = 'public' AND table_name = 'historial_precio_producto'
  ) AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'historial_precio_producto'
        AND column_name = 'presentacion_id'
  ) THEN
    ALTER TABLE historial_precio_producto
      ADD COLUMN presentacion_id BIGINT REFERENCES producto_presentacion (id);
    CREATE INDEX IF NOT EXISTS idx_hpp_presentacion
      ON historial_precio_producto (presentacion_id)
      WHERE presentacion_id IS NOT NULL;
  END IF;
END $$;

-- -----------------------------------------------------------------------------
-- Backfill mínimo (sprint 1): productos con precio_unidad → PAQUETE + UNIDAD
-- Factor de PAQUETE provisional = 1 (se corrige en migración final).
-- -----------------------------------------------------------------------------
INSERT INTO producto_presentacion (
    producto_id, codigo, nombre_mostrar, factor_a_base, precio_venta,
    es_default_venta, activo
)
SELECT
    p.id,
    'PAQUETE',
    'Paquete / presentación',
    1,
    COALESCE(p.precio, 0),
    TRUE,
    TRUE
FROM producto p
WHERE p.precio_unidad IS NOT NULL
  AND p.precio_unidad > 0
  AND NOT EXISTS (
      SELECT 1 FROM producto_presentacion pp
      WHERE pp.producto_id = p.id AND pp.codigo = 'PAQUETE'
  );

INSERT INTO producto_presentacion (
    producto_id, codigo, nombre_mostrar, factor_a_base, precio_venta,
    es_default_venta, activo
)
SELECT
    p.id,
    'UNIDAD',
    'Unidad (menudeo)',
    1,
    COALESCE(p.precio_unidad, 0),
    FALSE,
    TRUE
FROM producto p
WHERE p.precio_unidad IS NOT NULL
  AND p.precio_unidad > 0
  AND NOT EXISTS (
      SELECT 1 FROM producto_presentacion pp
      WHERE pp.producto_id = p.id AND pp.codigo = 'UNIDAD'
  );

-- Productos sin menudeo: al menos PAQUETE default (para que toda venta tenga UoM)
INSERT INTO producto_presentacion (
    producto_id, codigo, nombre_mostrar, factor_a_base, precio_venta,
    es_default_venta, activo
)
SELECT
    p.id,
    'PAQUETE',
    'Paquete / presentación',
    1,
    COALESCE(p.precio, 0),
    TRUE,
    TRUE
FROM producto p
WHERE (p.precio_unidad IS NULL OR p.precio_unidad <= 0)
  AND NOT EXISTS (
      SELECT 1 FROM producto_presentacion pp
      WHERE pp.producto_id = p.id AND pp.codigo = 'PAQUETE'
  );
