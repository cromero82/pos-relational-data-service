-- Metodo pago — descripcion_egreso, visible_pago_tickets, monto; quita es_base_proveedores

ALTER TABLE metodo_pago
    ADD COLUMN IF NOT EXISTS descripcion_egreso VARCHAR(100) NULL;

ALTER TABLE metodo_pago
    ADD COLUMN IF NOT EXISTS visible_pago_tickets BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE metodo_pago
    ADD COLUMN IF NOT EXISTS monto NUMERIC(12, 2) NULL;

UPDATE metodo_pago
SET visible_pago_tickets = TRUE
WHERE visible_pago_tickets IS NULL;

UPDATE metodo_pago
SET descripcion_egreso = descripcion
WHERE descripcion_egreso IS NULL;

UPDATE metodo_pago
SET
    descripcion_egreso = 'Caja: Efectivo',
    visible_pago_tickets = TRUE,
    visible_pagos_egresos = TRUE
WHERE id = 1;

UPDATE metodo_pago
SET
    descripcion_egreso = descripcion,
    visible_pago_tickets = TRUE,
    visible_pagos_egresos = TRUE
WHERE id IN (2, 3);

ALTER TABLE metodo_pago
    DROP COLUMN IF EXISTS es_base_proveedores;

INSERT INTO metodo_pago (
    id,
    descripcion,
    descripcion_egreso,
    estado,
    file,
    sigla,
    color,
    visible_pagos_egresos,
    visible_pago_tickets,
    monto
)
SELECT
    4,
    'Efectivo: base para proveedores',
    'Efectivo: base para proveedores',
    mp.estado,
    mp.file,
    'EFBP',
    mp.color,
    TRUE,
    FALSE,
    NULL
FROM metodo_pago mp
WHERE mp.id = 1
  AND NOT EXISTS (SELECT 1 FROM metodo_pago WHERE id = 4);

UPDATE metodo_pago
SET
    descripcion = 'Efectivo: base para proveedores',
    descripcion_egreso = 'Efectivo: base para proveedores',
    visible_pagos_egresos = TRUE,
    visible_pago_tickets = FALSE
WHERE id = 4;

SELECT setval(
    pg_get_serial_sequence('metodo_pago', 'id'),
    GREATEST((SELECT MAX(id) FROM metodo_pago), 1)
);
