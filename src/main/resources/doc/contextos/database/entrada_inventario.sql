-- Entradas de inventario (manual) vinculadas a egreso
-- Ejecutar en controlneg_rmx_db

ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS existencia INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS entrada_inventario (
    id                  BIGSERIAL PRIMARY KEY,
    egreso_id           INTEGER NOT NULL UNIQUE,
    estado              VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    fecha_creacion      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_confirmacion  TIMESTAMP WITHOUT TIME ZONE,
    usuario_id          UUID,
    observaciones       TEXT,
    total_items         INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_entrada_egreso FOREIGN KEY (egreso_id) REFERENCES egreso (id),
    CONSTRAINT chk_entrada_estado CHECK (estado IN ('BORRADOR', 'CONFIRMADA', 'ANULADA'))
);

CREATE TABLE IF NOT EXISTS entrada_inventario_detalle (
    id                          BIGSERIAL PRIMARY KEY,
    entrada_id                  BIGINT NOT NULL,
    producto_id                 BIGINT NOT NULL,
    cantidad                    INTEGER NOT NULL,
    precio_compra_registrado    NUMERIC(10, 2) NOT NULL,
    precio_compra_anterior      NUMERIC(10, 2),
    precio_venta_actual         NUMERIC(10, 2),
    precio_venta_nuevo          NUMERIC(10, 2),
    porcentaje_ganancia_calc    SMALLINT,
    porcentaje_variacion_compra NUMERIC(8, 2),
    alerta_precio_subio         BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eid_entrada FOREIGN KEY (entrada_id) REFERENCES entrada_inventario (id) ON DELETE CASCADE,
    CONSTRAINT fk_eid_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT chk_eid_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_eid_precio CHECK (precio_compra_registrado >= 0)
);

CREATE INDEX IF NOT EXISTS idx_entrada_egreso ON entrada_inventario (egreso_id);
CREATE INDEX IF NOT EXISTS idx_entrada_estado ON entrada_inventario (estado);
CREATE INDEX IF NOT EXISTS idx_eid_entrada ON entrada_inventario_detalle (entrada_id);
CREATE INDEX IF NOT EXISTS idx_eid_producto ON entrada_inventario_detalle (producto_id);

-- Migración: precio de venta a aplicar al confirmar (si difiere del catálogo)
ALTER TABLE entrada_inventario_detalle
    ADD COLUMN IF NOT EXISTS precio_venta_nuevo NUMERIC(10, 2);
