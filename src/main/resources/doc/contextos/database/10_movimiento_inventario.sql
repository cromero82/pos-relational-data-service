-- Sprint 3 — Movimientos de inventario (cabecera + detalle)
-- Ejecutar después de Sprint 0 (03_tipo_movimiento_inventario, 05_documento_venta)

CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id                      BIGSERIAL PRIMARY KEY,
    consecutivo             VARCHAR(30) NOT NULL,
    anio                    INTEGER NOT NULL,
    tipo_movimiento_id      BIGINT NOT NULL,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA',
    fecha_hecho             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id              UUID,
    documento_venta_id      BIGINT,
    nota_ajuste_id          BIGINT,
    entrada_inventario_id   BIGINT,
    historial_recibo_id     BIGINT,
    recibo_id               BIGINT,
    egreso_id               INTEGER,
    motivo_texto            TEXT,
    fecha_creacion          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mi_tipo FOREIGN KEY (tipo_movimiento_id) REFERENCES tipo_movimiento_inventario (id),
    CONSTRAINT fk_mi_documento_venta FOREIGN KEY (documento_venta_id) REFERENCES documento_venta (id),
    CONSTRAINT fk_mi_nota_ajuste FOREIGN KEY (nota_ajuste_id) REFERENCES nota_ajuste_documento (id),
    CONSTRAINT fk_mi_entrada FOREIGN KEY (entrada_inventario_id) REFERENCES entrada_inventario (id),
    CONSTRAINT fk_mi_historial FOREIGN KEY (historial_recibo_id) REFERENCES historial_recibo (id),
    CONSTRAINT fk_mi_egreso FOREIGN KEY (egreso_id) REFERENCES egreso (id),
    CONSTRAINT chk_mi_estado CHECK (estado IN ('BORRADOR', 'CONFIRMADA', 'ANULADA'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_movimiento_inventario_consecutivo_anio
    ON movimiento_inventario (anio, consecutivo);

CREATE INDEX IF NOT EXISTS idx_mi_historial ON movimiento_inventario (historial_recibo_id);
CREATE INDEX IF NOT EXISTS idx_mi_entrada ON movimiento_inventario (entrada_inventario_id);
CREATE INDEX IF NOT EXISTS idx_mi_tipo ON movimiento_inventario (tipo_movimiento_id);

CREATE TABLE IF NOT EXISTS movimiento_inventario_detalle (
    id                  BIGSERIAL PRIMARY KEY,
    movimiento_id       BIGINT NOT NULL,
    producto_id         BIGINT NOT NULL,
    cantidad            NUMERIC(12, 3) NOT NULL,
    cantidad_sistema    NUMERIC(12, 3),
    cantidad_contada    NUMERIC(12, 3),
    motivo_linea        TEXT,
    direccion_linea     VARCHAR(10) NOT NULL,
    CONSTRAINT fk_mid_movimiento FOREIGN KEY (movimiento_id) REFERENCES movimiento_inventario (id) ON DELETE CASCADE,
    CONSTRAINT fk_mid_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT chk_mid_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_mid_direccion CHECK (direccion_linea IN ('ENTRADA', 'SALIDA'))
);

CREATE INDEX IF NOT EXISTS idx_mid_movimiento ON movimiento_inventario_detalle (movimiento_id);
CREATE INDEX IF NOT EXISTS idx_mid_producto ON movimiento_inventario_detalle (producto_id);
