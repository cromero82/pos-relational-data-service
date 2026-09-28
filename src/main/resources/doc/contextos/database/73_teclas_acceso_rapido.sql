-- Atajos de teclado por funcionalidad (JSON en value).
-- Idempotente: no pisa value ni leyenda si la key ya existe.
-- Destino: controlneg_rmx_db_v02.

INSERT INTO configuracion_app (key, value, leyenda)
SELECT
    'teclas-acceso-rapido',
    '[{"funcionalidad":"Tickets","map":[{"elemento":"Metodo de pago > efectivo","combinacion":"[Shift] + 1"},{"elemento":"Metodo de pago > bancolombia-qr","combinacion":"[Shift] + 2"}]}]',
    'Atajos de teclado por funcionalidad (JSON)'
WHERE NOT EXISTS (
    SELECT 1 FROM configuracion_app c WHERE c.key = 'teclas-acceso-rapido'
);

SELECT setval(
    'configuracion_app_id_seq',
    GREATEST((SELECT MAX(id) FROM configuracion_app), 1)
);
