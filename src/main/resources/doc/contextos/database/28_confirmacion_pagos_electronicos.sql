-- Confirmación pagos electrónicos (email Bancolombia / QR)
-- BD: controlneg_rmx_db

-- 1) Parámetros por establecimiento
ALTER TABLE establecimiento
    ADD COLUMN IF NOT EXISTS referencia_cuenta_qr VARCHAR(4);

ALTER TABLE establecimiento
    ADD COLUMN IF NOT EXISTS email_alerta_pagos VARCHAR(255);

COMMENT ON COLUMN establecimiento.referencia_cuenta_qr IS
    'Últimos 4 dígitos de la cuenta QR (ej. 3861) para validar alertas bancarias.';
COMMENT ON COLUMN establecimiento.email_alerta_pagos IS
    'Correo de alerta bancaria / CF Email Routing (ej. pagos@mayaksoluciones.com).';

UPDATE establecimiento
SET
    referencia_cuenta_qr = COALESCE(referencia_cuenta_qr, '3861'),
    email_alerta_pagos = 'pagos@mayaksoluciones.com'
WHERE id = 1;

-- 2) Plantilla de parseo por método de pago
ALTER TABLE metodo_pago
    ADD COLUMN IF NOT EXISTS plantilla_notificacion_pago TEXT;

COMMENT ON COLUMN metodo_pago.plantilla_notificacion_pago IS
    'Plantilla del cuerpo del email con tags {{nombrePagador}}, {{monto}}, {{referenciaCuenta}}.';

UPDATE metodo_pago
SET plantilla_notificacion_pago =
    'Bancolombia: CARLOS, recibiste una transferencia de {{nombrePagador}} por {{monto}} en tu cuenta *{{referenciaCuenta}} conectada a la llave 86070384 el 31/07/26 a las 20:34. Con llaves es de una y gratis. Dudas al 018000912345'
WHERE id = 2
  AND (plantilla_notificacion_pago IS NULL OR plantilla_notificacion_pago = '');

-- 3) Pendientes de confirmación electrónica (1:1 con historial_recibo de pagos QR)
CREATE TABLE IF NOT EXISTS historial_recibos_electronicos (
    id                      BIGSERIAL PRIMARY KEY,
    historial_recibo_id     BIGINT NOT NULL REFERENCES historial_recibo (id),
    sesion_id               BIGINT,
    metodo_pago_id          BIGINT REFERENCES metodo_pago (id),
    monto_esperado          NUMERIC(12, 2) NOT NULL,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'CREADA',
    nombre_pagador          VARCHAR(200),
    fecha_creacion          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_confirmacion      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT ck_hre_estado CHECK (estado IN ('CREADA', 'CONFIRMADA', 'AMBIGUA', 'HUERFANA')),
    CONSTRAINT uq_hre_historial UNIQUE (historial_recibo_id)
);

CREATE INDEX IF NOT EXISTS idx_hre_estado_sesion_monto
    ON historial_recibos_electronicos (estado, sesion_id, monto_esperado);

CREATE INDEX IF NOT EXISTS idx_hre_historial
    ON historial_recibos_electronicos (historial_recibo_id);

-- 4) Cola de emails inbound (webhook Cloudflare)
CREATE TABLE IF NOT EXISTS notificacion_email_pago (
    id                              BIGSERIAL PRIMARY KEY,
    message_id                      VARCHAR(255),
    recibido_en                     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asunto                          VARCHAR(500),
    cuerpo_raw                      TEXT,
    cuerpo_texto                    TEXT,
    monto                           NUMERIC(12, 2),
    nombre_pagador                  VARCHAR(200),
    referencia_cuenta               VARCHAR(4),
    metodo_pago_id                  BIGINT REFERENCES metodo_pago (id),
    estado_vista                    VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    historial_recibo_electronico_id BIGINT REFERENCES historial_recibos_electronicos (id),
    CONSTRAINT ck_nep_estado_vista CHECK (estado_vista IN ('PENDIENTE', 'MOSTRADA')),
    CONSTRAINT uq_nep_message_id UNIQUE (message_id)
);

CREATE INDEX IF NOT EXISTS idx_nep_estado_vista
    ON notificacion_email_pago (estado_vista, recibido_en DESC);
