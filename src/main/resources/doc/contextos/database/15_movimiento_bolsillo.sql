-- Sprint B1 — Libro de movimientos de bolsillos

CREATE TABLE IF NOT EXISTS movimiento_bolsillo (
    id                      BIGSERIAL PRIMARY KEY,
    fecha                   DATE NOT NULL,
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id              VARCHAR(36) NOT NULL,
    cuenta_bolsillo_id      INTEGER NOT NULL REFERENCES cuenta_bolsillo (id),
    cuenta_destino_id       INTEGER NULL REFERENCES cuenta_bolsillo (id),
    tipo_movimiento         VARCHAR(40) NOT NULL,
    valor                   NUMERIC(14, 2) NOT NULL CHECK (valor >= 0),
    impacto                 NUMERIC(14, 2) NOT NULL,
    saldo_antes             NUMERIC(14, 2) NOT NULL,
    saldo_despues           NUMERIC(14, 2) NOT NULL,
    metodo_pago_id          BIGINT NULL REFERENCES metodo_pago (id),
    tercero_nombre          VARCHAR(150),
    motivo_movimiento_id    INTEGER NULL REFERENCES motivo_movimiento (id),
    observacion             TEXT,
    valor_sistema           NUMERIC(14, 2),
    valor_real              NUMERIC(14, 2),
    origen_tipo             VARCHAR(30),
    origen_id               BIGINT,
    grupo_traslado_id       VARCHAR(36),
    -- Solo la pata de salida (impacto < 0) exige destino; la entrada puede ser NULL.
    CONSTRAINT chk_movimiento_traslado_destino
        CHECK (
            tipo_movimiento <> 'TRASLADO'
            OR impacto >= 0
            OR cuenta_destino_id IS NOT NULL
        )
);

CREATE INDEX IF NOT EXISTS idx_movimiento_cuenta ON movimiento_bolsillo (cuenta_bolsillo_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_fecha ON movimiento_bolsillo (fecha);
CREATE INDEX IF NOT EXISTS idx_movimiento_grupo_traslado ON movimiento_bolsillo (grupo_traslado_id);
