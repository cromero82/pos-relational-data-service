-- Capa documental ventas (Sprint 1)
-- Ejecutar después de 00, 01, 02

CREATE TABLE IF NOT EXISTS documento_venta (
    id                      BIGSERIAL PRIMARY KEY,
    consecutivo             VARCHAR(30) NOT NULL,
    anio                    INTEGER NOT NULL,
    historial_recibo_id     BIGINT NOT NULL UNIQUE,
    fecha_hecho             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total                   NUMERIC(12, 2) NOT NULL,
    metodo_pago_id          BIGINT,
    cliente_id              BIGINT NOT NULL,
    usuario_id              UUID,
    sesion_id               BIGINT,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'VIGENTE',
    nota_ajuste_anulacion_id BIGINT,
    fecha_creacion          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dv_historial FOREIGN KEY (historial_recibo_id) REFERENCES historial_recibo (id),
    CONSTRAINT fk_dv_metodo_pago FOREIGN KEY (metodo_pago_id) REFERENCES metodo_pago (id),
    CONSTRAINT fk_dv_cliente FOREIGN KEY (cliente_id) REFERENCES client (id),
    CONSTRAINT chk_dv_estado CHECK (estado IN ('VIGENTE', 'ANULADO'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_documento_venta_consecutivo_anio
    ON documento_venta (anio, consecutivo);

CREATE INDEX IF NOT EXISTS idx_documento_venta_historial ON documento_venta (historial_recibo_id);
CREATE INDEX IF NOT EXISTS idx_documento_venta_estado ON documento_venta (estado);

ALTER TABLE historial_recibo
    ADD COLUMN IF NOT EXISTS documento_venta_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_historial_documento_venta'
    ) THEN
        ALTER TABLE historial_recibo
            ADD CONSTRAINT fk_historial_documento_venta
            FOREIGN KEY (documento_venta_id) REFERENCES documento_venta (id);
    END IF;
END $$;

-- Tablas NC/ND (estructura Sprint 2; vacías hasta anular/restaurar)
CREATE TABLE IF NOT EXISTS nota_ajuste_documento (
    id                          BIGSERIAL PRIMARY KEY,
    tipo                        VARCHAR(10) NOT NULL,
    consecutivo                 VARCHAR(30) NOT NULL,
    anio                        INTEGER NOT NULL,
    documento_venta_origen_id   BIGINT NOT NULL,
    motivo_operacion_id         BIGINT,
    motivo_texto                TEXT,
    total_ajuste                NUMERIC(12, 2) NOT NULL,
    fecha_hecho                 TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id                  UUID,
    historial_recibo_id         BIGINT,
    operacion_restauracion      BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nad_documento_origen FOREIGN KEY (documento_venta_origen_id) REFERENCES documento_venta (id),
    CONSTRAINT fk_nad_motivo FOREIGN KEY (motivo_operacion_id) REFERENCES motivo_operacion (id),
    CONSTRAINT chk_nad_tipo CHECK (tipo IN ('CREDITO', 'DEBITO'))
);

CREATE TABLE IF NOT EXISTS nota_ajuste_detalle (
    id                  BIGSERIAL PRIMARY KEY,
    nota_ajuste_id      BIGINT NOT NULL,
    producto_id         BIGINT,
    cantidad            NUMERIC(12, 3),
    valor_unitario      NUMERIC(12, 2),
    subtotal            NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_nad_det_nota FOREIGN KEY (nota_ajuste_id) REFERENCES nota_ajuste_documento (id) ON DELETE CASCADE,
    CONSTRAINT fk_nad_det_producto FOREIGN KEY (producto_id) REFERENCES producto (id)
);
