-- 37 — CxC / abonos (schema estable; UI incremental)
-- Cobranza ≠ Ventas del día. Abonos alimentan caja/OF, no totalVentasSistema.

CREATE TABLE IF NOT EXISTS cuenta_por_cobrar (
    id                      BIGSERIAL PRIMARY KEY,
    historial_recibo_id     BIGINT REFERENCES historial_recibo (id),
    documento_venta_id      BIGINT REFERENCES documento_venta (id),
    cliente_id              BIGINT NOT NULL REFERENCES client (id),
    fecha_origen            TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_original          NUMERIC(12, 2) NOT NULL,
    saldo_pendiente         NUMERIC(12, 2) NOT NULL,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    observacion             TEXT,
    usuario_id              UUID,
    fecha_creacion          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_cxc_montos CHECK (monto_original > 0 AND saldo_pendiente >= 0),
    CONSTRAINT ck_cxc_estado CHECK (estado IN ('ABIERTA', 'PARCIAL', 'PAGADA', 'ANULADA'))
);

CREATE INDEX IF NOT EXISTS idx_cxc_cliente ON cuenta_por_cobrar (cliente_id);
CREATE INDEX IF NOT EXISTS idx_cxc_estado ON cuenta_por_cobrar (estado);
CREATE INDEX IF NOT EXISTS idx_cxc_historial ON cuenta_por_cobrar (historial_recibo_id);

COMMENT ON TABLE cuenta_por_cobrar IS
    'Deuda de cliente (crédito interno / DIAN forma 2 futuro). '
    'No suma a Ventas del corte hasta cobrarse; el abono es cobranza.';

CREATE TABLE IF NOT EXISTS abono_cxc (
    id                      BIGSERIAL PRIMARY KEY,
    cuenta_por_cobrar_id    BIGINT NOT NULL REFERENCES cuenta_por_cobrar (id),
    fecha_abono             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto                   NUMERIC(12, 2) NOT NULL,
    metodo_pago_id          BIGINT NOT NULL REFERENCES metodo_pago (id),
    origen_fondos_id        INTEGER REFERENCES origen_fondos (id),
    movimiento_origen_fondos_id BIGINT,
    usuario_id              UUID,
    observacion             TEXT,
    fecha_creacion          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_abono_monto CHECK (monto > 0)
);

CREATE INDEX IF NOT EXISTS idx_abono_cxc_cuenta ON abono_cxc (cuenta_por_cobrar_id);
CREATE INDEX IF NOT EXISTS idx_abono_cxc_fecha ON abono_cxc (fecha_abono);

COMMENT ON TABLE abono_cxc IS
    'Cobranza parcial/total. Entra a caja/OF del medio; dashboard debe reportar '
    'como Cobranza, no como Ventas del día del abono.';
