-- Alinear catálogo estado_recibos con ReciboEstado (Java ids 1-4)
-- Idempotente: actualiza siglas/descripciones sin cambiar ids usados en historial_recibo

UPDATE estado_recibos SET sigla = 'PEN', descripcion = 'PENDIENTE PAGO'
WHERE id = 1 AND (sigla IS DISTINCT FROM 'PEN' OR descripcion IS DISTINCT FROM 'PENDIENTE PAGO');

UPDATE estado_recibos SET sigla = 'PAG', descripcion = 'PAGADO'
WHERE id = 2 AND (sigla IS DISTINCT FROM 'PAG' OR descripcion IS DISTINCT FROM 'PAGADO');

UPDATE estado_recibos SET sigla = 'AN', descripcion = 'ANULADO'
WHERE id = 3 AND (sigla IS DISTINCT FROM 'AN' OR descripcion IS DISTINCT FROM 'ANULADO');

UPDATE estado_recibos SET sigla = 'ED', descripcion = 'EDICION'
WHERE id = 4 AND (sigla IS DISTINCT FROM 'ED' OR descripcion IS DISTINCT FROM 'EDICION');

-- Insertar faltantes si la tabla está vacía o incompleta
INSERT INTO estado_recibos (id, sigla, descripcion)
VALUES
    (1, 'PEN', 'PENDIENTE PAGO'),
    (2, 'PAG', 'PAGADO'),
    (3, 'AN', 'ANULADO'),
    (4, 'ED', 'EDICION')
ON CONFLICT (id) DO UPDATE SET
    sigla = EXCLUDED.sigla,
    descripcion = EXCLUDED.descripcion;

SELECT setval(pg_get_serial_sequence('estado_recibos', 'id'), GREATEST((SELECT MAX(id) FROM estado_recibos), 4));
