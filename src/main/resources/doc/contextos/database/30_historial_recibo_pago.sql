-- 30 — Multipago: líneas de cobro por historial + código DIAN PaymentMeansCode
-- Idempotente. Backfill 1:1 desde historial_recibo (ventas históricas = un solo medio).
-- Fuente de verdad de montos por medio: historial_recibo_pago (no recibo_pago ni documento_venta_pago).

-- ---------------------------------------------------------------------------
-- 1) Catálogo: código DIAN (lista medios de pago / UNCL4461 adaptada por DIAN)
-- ---------------------------------------------------------------------------
ALTER TABLE metodo_pago
    ADD COLUMN IF NOT EXISTS codigo_dian_payment_means VARCHAR(3);

COMMENT ON COLUMN metodo_pago.codigo_dian_payment_means IS
    'PaymentMeansCode DIAN (ej. 10=Efectivo, 45=Transferencia crédito bancario). '
    'Usado al emitir XML; NULL si el medio no aplica a facturación de tickets.';

-- Mapeo inicial (revisar con proveedor tecnológico antes de producción DIAN)
UPDATE metodo_pago SET codigo_dian_payment_means = '10' WHERE id = 1 AND codigo_dian_payment_means IS NULL; -- Efectivo
UPDATE metodo_pago SET codigo_dian_payment_means = '45' WHERE id = 2 AND codigo_dian_payment_means IS NULL; -- Bancolombia QR
UPDATE metodo_pago SET codigo_dian_payment_means = '45' WHERE id = 3 AND codigo_dian_payment_means IS NULL; -- Nequi
-- id=4 Efectivo base proveedores: sin código (no visible en tickets)

-- ---------------------------------------------------------------------------
-- 2) Líneas de pago del historial (venta ya cobrada)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_recibo_pago (
    id                      BIGSERIAL PRIMARY KEY,
    historial_recibo_id     BIGINT NOT NULL REFERENCES historial_recibo (id),
    metodo_pago_id          BIGINT NOT NULL REFERENCES metodo_pago (id),
    monto                   NUMERIC(12, 2) NOT NULL,
    orden                   SMALLINT NOT NULL DEFAULT 1,
    fecha_creacion          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_hrp_monto_positivo CHECK (monto > 0),
    CONSTRAINT ck_hrp_orden_positivo CHECK (orden >= 1),
    CONSTRAINT uq_hrp_historial_orden UNIQUE (historial_recibo_id, orden)
);

CREATE INDEX IF NOT EXISTS idx_hrp_historial
    ON historial_recibo_pago (historial_recibo_id);

CREATE INDEX IF NOT EXISTS idx_hrp_metodo_pago
    ON historial_recibo_pago (metodo_pago_id);

COMMENT ON TABLE historial_recibo_pago IS
    'Desglose de cobro por medio. SUM(monto) debe igualar historial_recibo.total '
    '(salvo reglas futuras de cambio/propina). Corte y ENTRADA_VENTA agregan desde aquí.';

COMMENT ON COLUMN historial_recibo_pago.orden IS
    'Orden de captura en caja (1..N). Único por historial.';

-- ---------------------------------------------------------------------------
-- 3) Backfill: un pago = total del historial (compatibilidad corte actual)
-- ---------------------------------------------------------------------------
INSERT INTO historial_recibo_pago (historial_recibo_id, metodo_pago_id, monto, orden)
SELECT
    h.id,
    h.metodo_pago_id,
    h.total,
    1
FROM historial_recibo h
WHERE h.metodo_pago_id IS NOT NULL
  AND h.total IS NOT NULL
  AND h.total > 0
  AND NOT EXISTS (
      SELECT 1
      FROM historial_recibo_pago p
      WHERE p.historial_recibo_id = h.id
  );

-- Verificación suave (informativa): historiales con MP sin líneas
-- SELECT h.id FROM historial_recibo h
-- LEFT JOIN historial_recibo_pago p ON p.historial_recibo_id = h.id
-- WHERE h.metodo_pago_id IS NOT NULL AND p.id IS NULL;
