-- Tickets electrónicos sin confirmación (caja: "Ya no esperar")
-- BD: controlneg_rmx_db

CREATE TABLE IF NOT EXISTS ticket_sin_notificacion (
    id                                  BIGSERIAL PRIMARY KEY,
    historial_recibo_id                 BIGINT NOT NULL REFERENCES historial_recibo (id),
    historial_recibo_electronico_id     BIGINT REFERENCES historial_recibos_electronicos (id) ON DELETE SET NULL,
    numero_venta                        VARCHAR(40),
    valor                               NUMERIC(12, 2) NOT NULL,
    fecha                               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    persona                             VARCHAR(200),
    marcado_en                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tsn_historial_recibo UNIQUE (historial_recibo_id)
);

CREATE INDEX IF NOT EXISTS idx_tsn_marcado_en
    ON ticket_sin_notificacion (marcado_en DESC);

COMMENT ON TABLE ticket_sin_notificacion IS
    'Ventas QR/email en las que la caja dejó de esperar confirmación electrónica. historial_recibo_id = recibo.id.';
