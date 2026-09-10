-- 57 — Egreso con orígenes múltiples (1:N)
-- Un egreso puede pagarse desde varios orígenes de fondos (p.ej. Caja menor + Caja + Bancolombia QR).
-- Cada línea tiene su valor; la suma debe coincidir con egreso.valor.
-- No hay backfill: en producción no hay egresos registrados.
-- egreso.origen_fondos_id / metodo_pago_id se conservan como snapshot del primer origen (listados / compat).

CREATE TABLE IF NOT EXISTS egreso_origen_fondos (
    id              BIGSERIAL PRIMARY KEY,
    egreso_id       BIGINT NOT NULL REFERENCES egreso (id) ON DELETE CASCADE,
    origen_fondos_id INTEGER NOT NULL REFERENCES origen_fondos (id),
    metodo_pago_id  BIGINT NULL REFERENCES metodo_pago (id),
    valor           NUMERIC(15, 2) NOT NULL,
    orden           SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_egreso_origen_fondos_valor_positivo CHECK (valor > 0),
    CONSTRAINT uq_egreso_origen_fondos UNIQUE (egreso_id, origen_fondos_id)
);

CREATE INDEX IF NOT EXISTS idx_egreso_origen_fondos_egreso
    ON egreso_origen_fondos (egreso_id);

CREATE INDEX IF NOT EXISTS idx_egreso_origen_fondos_origen
    ON egreso_origen_fondos (origen_fondos_id);

COMMENT ON TABLE egreso_origen_fondos IS
    'Orígenes de un egreso (1:N). Cada fila descuenta ese valor del origen de fondos.';

COMMENT ON COLUMN egreso_origen_fondos.valor IS
    'Monto descontado de este origen. La suma de líneas = egreso.valor.';

COMMENT ON COLUMN egreso.origen_fondos_id IS
    'Snapshot del primer origen (compatibilidad). La fuente de verdad es egreso_origen_fondos.';

COMMENT ON COLUMN egreso.metodo_pago_id IS
    'Snapshot del método de pago del primer origen (compatibilidad).';
