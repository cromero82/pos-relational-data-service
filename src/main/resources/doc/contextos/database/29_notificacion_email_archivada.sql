-- Archivar notificaciones email de pago (gestión admin POS)
-- BD: controlneg_rmx_db

ALTER TABLE notificacion_email_pago DROP CONSTRAINT IF EXISTS ck_nep_estado_vista;

ALTER TABLE notificacion_email_pago
    ADD CONSTRAINT ck_nep_estado_vista
    CHECK (estado_vista::text = ANY (ARRAY[
        'PENDIENTE'::character varying,
        'MOSTRADA'::character varying,
        'ARCHIVADA'::character varying
    ]::text[]));

COMMENT ON COLUMN notificacion_email_pago.estado_vista IS
    'PENDIENTE | MOSTRADA (ya vista en panel caja) | ARCHIVADA (gestión admin).';
