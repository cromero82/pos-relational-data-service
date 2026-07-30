-- Sprint 3 — Libro auxiliar kardex (append-only)
-- Ejecutar después de 10_movimiento_inventario.sql

CREATE TABLE IF NOT EXISTS inventario_kardex (
    id                      BIGSERIAL PRIMARY KEY,
    producto_id             BIGINT NOT NULL,
    movimiento_detalle_id   BIGINT NOT NULL,
    fecha_hecho             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad_entrada        NUMERIC(12, 3) NOT NULL DEFAULT 0,
    cantidad_salida         NUMERIC(12, 3) NOT NULL DEFAULT 0,
    saldo_resultante        NUMERIC(12, 3) NOT NULL,
    usuario_id              UUID,
    fecha_creacion          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kardex_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_kardex_detalle FOREIGN KEY (movimiento_detalle_id) REFERENCES movimiento_inventario_detalle (id),
    CONSTRAINT chk_kardex_mov CHECK (cantidad_entrada >= 0 AND cantidad_salida >= 0)
);

CREATE INDEX IF NOT EXISTS idx_kardex_producto_fecha
    ON inventario_kardex (producto_id, fecha_hecho DESC, id DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uq_kardex_movimiento_detalle
    ON inventario_kardex (movimiento_detalle_id);
