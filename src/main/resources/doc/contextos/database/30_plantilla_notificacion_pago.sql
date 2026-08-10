-- Plantillas 1:N para extracción de emails de pago (QR / BREVE / OTRO)
-- BD: controlneg_rmx_db

CREATE TABLE IF NOT EXISTS plantilla_notificacion_pago (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(40) NOT NULL,
    cuerpo          TEXT NOT NULL,
    icono           VARCHAR(120) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    orden           INTEGER NOT NULL DEFAULT 0,
    creado_en       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pnp_nombre UNIQUE (nombre)
);

CREATE INDEX IF NOT EXISTS idx_pnp_activo_orden
    ON plantilla_notificacion_pago (activo, orden);

COMMENT ON TABLE plantilla_notificacion_pago IS
    'Plantillas de extracción del cuerpo de email (QR, BREVE, OTRO). icono = archivo en assets/iconos/metodos-pago.';

ALTER TABLE notificacion_email_pago
    ADD COLUMN IF NOT EXISTS plantilla_notificacion_id BIGINT REFERENCES plantilla_notificacion_pago (id) ON DELETE SET NULL;

ALTER TABLE notificacion_email_pago
    ADD COLUMN IF NOT EXISTS plantilla_nombre VARCHAR(40);

ALTER TABLE notificacion_email_pago
    ADD COLUMN IF NOT EXISTS plantilla_icono VARCHAR(120);

INSERT INTO plantilla_notificacion_pago (nombre, cuerpo, icono, activo, orden)
VALUES
(
    'QR',
    'Bancolombia: AUTOSERVICIO INFINITO, recibiste un pago de {{nombrePagador}} por {{monto}} en tu cuenta *{{referenciaCuenta}} conectado a la llave 0072617673 el 09/08/2026 a las 10:53. Con codigo QR es facil y de una. Dudas al 018000912345.',
    'qr-bancolombia.png',
    TRUE,
    1
),
(
    'BREVE',
    'Bancolombia: CARLOS, recibiste una transferencia de {{nombrePagador}} por {{monto}} en tu cuenta *{{referenciaCuenta}} conectada a la llave 86070384 el 31/07/26 a las 20:34. Con llaves es de una y gratis. Dudas al 018000912345',
    'breve-logo.png',
    TRUE,
    2
),
(
    'OTRO',
    'recibiste una transferencia de {{nombrePagador}} por {{monto}} en tu cuenta *{{referenciaCuenta}}',
    'otro-metodo.png',
    TRUE,
    3
)
ON CONFLICT (nombre) DO NOTHING;
