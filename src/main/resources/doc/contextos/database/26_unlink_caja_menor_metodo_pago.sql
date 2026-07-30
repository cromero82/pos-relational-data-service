-- Caja Menor / Caja General son bolsillos internos, no medios de pago de tickets.
-- Estaban mal enlazados a metodo_pago id 4 ("Efectivo: base para proveedores"),
-- lo que contaminaba el cierre de ventas con egresos/traslados de Caja Menor.

UPDATE origen_fondos
SET metodo_pago_id = NULL
WHERE nombre ILIKE 'Caja Menor'
  AND metodo_pago_id IS NOT NULL;

UPDATE origen_fondos
SET metodo_pago_id = NULL
WHERE (nombre ILIKE 'Caja General' OR nombre ILIKE 'Caja general%')
  AND metodo_pago_id IS NOT NULL;

-- Legacy: ya no es medio de tickets ni de egresos POS.
UPDATE metodo_pago
SET estado = 'inactivo',
    visible_pago_tickets = FALSE,
    visible_pagos_egresos = FALSE
WHERE id = 4
  AND descripcion ILIKE '%base para proveedores%';

-- Limpiar filas operativas ya contaminadas (mismo establecimiento de prueba).
UPDATE egreso e
SET metodo_pago_id = NULL
WHERE e.origen_fondos_id IN (
    SELECT id FROM origen_fondos WHERE metodo_pago_id IS NULL
)
AND e.metodo_pago_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM origen_fondos o
    WHERE o.id = e.origen_fondos_id AND o.metodo_pago_id = e.metodo_pago_id
);

UPDATE movimiento_origen_fondos m
SET metodo_pago_id = NULL
WHERE m.origen_fondos_id IN (
    SELECT id FROM origen_fondos WHERE metodo_pago_id IS NULL
)
AND m.metodo_pago_id IS NOT NULL;

COMMENT ON COLUMN origen_fondos.metodo_pago_id IS
    'Solo orígenes que reciben ventas POS (Efectivo, QR, Nequi...). Bolsillos internos (Caja Menor/General) deben ser NULL.';
