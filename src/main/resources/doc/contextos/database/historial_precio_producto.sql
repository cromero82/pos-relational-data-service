-- Historial de cambios de precio (compra/venta) originados en entradas de inventario
-- Ejecutar en controlneg_rmx_db

CREATE TABLE IF NOT EXISTS historial_precio_producto (
    id                              BIGSERIAL PRIMARY KEY,
    entrada_inventario_detalle_id   BIGINT NOT NULL,
    producto_id                     BIGINT NOT NULL,
    usuario_id                      UUID,
    fecha_creacion                  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    precio_compra                   NUMERIC(10, 2),
    precio_compra_antes             NUMERIC(10, 2),
    precio_venta                    NUMERIC(10, 2),
    precio_venta_antes              NUMERIC(10, 2),
    porcentaje_ganancia             SMALLINT,
    porcentaje_ganancia_antes       SMALLINT,
    CONSTRAINT fk_hpp_detalle FOREIGN KEY (entrada_inventario_detalle_id)
        REFERENCES entrada_inventario_detalle (id) ON DELETE CASCADE,
    CONSTRAINT fk_hpp_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id)
);

CREATE INDEX IF NOT EXISTS idx_hpp_producto_fecha
    ON historial_precio_producto (producto_id, fecha_creacion DESC);

CREATE INDEX IF NOT EXISTS idx_hpp_detalle
    ON historial_precio_producto (entrada_inventario_detalle_id);

-- Evento bitácora (si no existe)
INSERT INTO evento (nombre, sigla)
SELECT 'Modificar precio producto', 'MOD_PRC_PROD'
WHERE NOT EXISTS (SELECT 1 FROM evento WHERE sigla = 'MOD_PRC_PROD');

INSERT INTO evento (nombre, sigla)
SELECT 'Entrada inventario precios', 'ENTRADA_INV_PRECIO'
WHERE NOT EXISTS (SELECT 1 FROM evento WHERE sigla = 'ENTRADA_INV_PRECIO');
