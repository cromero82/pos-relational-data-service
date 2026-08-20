-- 40 — Renombrar bolsa «Para ordenar» → «Sin Clasificar»
-- Misma cuenta (id típico 8, hijo de Bancolombia QR); solo cambia el label operativo.

UPDATE origen_fondos
SET nombre = 'Sin Clasificar'
WHERE LOWER(TRIM(nombre)) = LOWER('Para ordenar');

COMMENT ON TABLE origen_fondos IS
    'Orígenes de fondos. Bolsa de pagos por identificar: «Sin Clasificar» (ex «Para ordenar»).';
