-- Alinea bolsillos internos al catálogo de la laptop (controlneg_rmx_db):
--   origen_fondos id=4  Caja Menor   (FISICA, sin metodo_pago)
--   origen_fondos id=5  Caja General (FISICA, sin metodo_pago)
--
-- Causa: 14_cuenta_bolsillo.sql crea un OF por cada metodo_pago. En prod,
-- metodo_pago id=4 se llama "Efectivo: base para proveedores" y además inserta
-- "Reserva pago proveedores". 26_ solo desvincula si el nombre YA es
-- Caja Menor / Caja General, así que en v02 quedaban los labels viejos,
-- OF 4 ligado a mp 4 y naturaleza ELECTRONICA.
--
-- Idempotente. Solo destino v02 / prod v02. No ejecutar sobre controlneg_rmx_db.

UPDATE origen_fondos
SET nombre = 'Caja Menor',
    metodo_pago_id = NULL,
    naturaleza = 'FISICA',
    visible_en_egreso = TRUE,
    activo = TRUE,
    orden = 4,
    estado = 'ACTIVO'
WHERE id = 4
   OR nombre ILIKE 'Efectivo: base para proveedores';

UPDATE origen_fondos
SET nombre = 'Caja General',
    metodo_pago_id = NULL,
    naturaleza = 'FISICA',
    visible_en_egreso = TRUE,
    activo = TRUE,
    orden = 100,
    estado = 'ACTIVO'
WHERE id = 5
   OR nombre ILIKE 'Reserva pago proveedores';

-- Limpieza igual que 26_: egresos/movimientos de estos bolsillos no heredan mp 4.
UPDATE egreso e
SET metodo_pago_id = NULL
WHERE e.origen_fondos_id IN (
    SELECT id FROM origen_fondos
    WHERE nombre ILIKE 'Caja Menor' OR nombre ILIKE 'Caja General'
)
AND e.metodo_pago_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM origen_fondos o
    WHERE o.id = e.origen_fondos_id AND o.metodo_pago_id = e.metodo_pago_id
);

UPDATE movimiento_origen_fondos m
SET metodo_pago_id = NULL
WHERE m.origen_fondos_id IN (
    SELECT id FROM origen_fondos
    WHERE nombre ILIKE 'Caja Menor' OR nombre ILIKE 'Caja General'
)
AND m.metodo_pago_id IS NOT NULL;
