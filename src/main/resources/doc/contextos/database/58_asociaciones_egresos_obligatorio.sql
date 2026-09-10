-- ¿El egreso desde un origen con plantilla (p.ej. QR) exige notificación de pago (correo / movimiento en bolsa)?
-- true  = no se puede registrar hasta identificar el movimiento (comportamiento histórico)
-- false = se puede registrar sin notificación; el dinero sale del origen elegido
INSERT INTO configuracion_app (key, value)
SELECT 'notificaciones.asociaciones-egresos.obligatorio', 'false'
WHERE NOT EXISTS (
    SELECT 1 FROM configuracion_app
    WHERE key = 'notificaciones.asociaciones-egresos.obligatorio'
);
