-- Plantillas de extracción: movimiento OF asociado (naturaleza + origen/destino)
-- BD: controlneg_rmx_db

ALTER TABLE plantilla_notificacion_pago
    ADD COLUMN IF NOT EXISTS naturaleza VARCHAR(20);

ALTER TABLE plantilla_notificacion_pago
    ADD COLUMN IF NOT EXISTS origen_fondos_origen_id INTEGER REFERENCES origen_fondos (id) ON DELETE SET NULL;

ALTER TABLE plantilla_notificacion_pago
    ADD COLUMN IF NOT EXISTS origen_fondos_destino_id INTEGER REFERENCES origen_fondos (id) ON DELETE SET NULL;

ALTER TABLE plantilla_notificacion_pago
    ADD COLUMN IF NOT EXISTS origen_tipo VARCHAR(80) NOT NULL DEFAULT 'MOVIMIENTO BANCO POR IDENTIFICAR';

UPDATE plantilla_notificacion_pago
SET origen_tipo = 'MOVIMIENTO BANCO POR IDENTIFICAR'
WHERE origen_tipo IS NULL OR origen_tipo = '';

ALTER TABLE plantilla_notificacion_pago
    DROP CONSTRAINT IF EXISTS ck_pnp_naturaleza;
ALTER TABLE plantilla_notificacion_pago
    ADD CONSTRAINT ck_pnp_naturaleza
        CHECK (naturaleza IS NULL OR naturaleza IN ('INGRESO', 'EGRESO'));

COMMENT ON COLUMN plantilla_notificacion_pago.naturaleza IS
    'Naturaleza del movimiento a registrar: INGRESO o EGRESO.';
COMMENT ON COLUMN plantilla_notificacion_pago.origen_fondos_origen_id IS
    'Origen de fondos origen (incluye OF hijos).';
COMMENT ON COLUMN plantilla_notificacion_pago.origen_fondos_destino_id IS
    'Origen de fondos destino (incluye OF hijos).';
COMMENT ON COLUMN plantilla_notificacion_pago.origen_tipo IS
    'origen_tipo del movimiento OF. Default MOVIMIENTO BANCO POR IDENTIFICAR.';

-- Reservar espacio para el mismo valor al crear el movimiento.
ALTER TABLE movimiento_origen_fondos
    ALTER COLUMN origen_tipo TYPE VARCHAR(80);
