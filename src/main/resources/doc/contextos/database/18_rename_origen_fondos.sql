-- Renombrado de dominio: bolsillos → orígenes de fondos
-- Medio de pago (metodo_pago) se mantiene para tickets.
-- Origen de fondos = dónde está / de dónde sale el dinero (ex cuenta_bolsillo).

-- 1) Catálogo de tipos
ALTER TABLE IF EXISTS tipo_bolsillo RENAME TO tipo_origen_fondos;
ALTER INDEX IF EXISTS tipo_bolsillo_pkey RENAME TO tipo_origen_fondos_pkey;
ALTER INDEX IF EXISTS tipo_bolsillo_codigo_key RENAME TO tipo_origen_fondos_codigo_key;

-- 2) Orígenes de fondos (ex cuenta_bolsillo)
ALTER TABLE IF EXISTS cuenta_bolsillo RENAME TO origen_fondos;
ALTER TABLE origen_fondos RENAME COLUMN tipo_bolsillo_id TO tipo_origen_fondos_id;
ALTER TABLE origen_fondos RENAME COLUMN parent_cuenta_bolsillo_id TO parent_origen_fondos_id;

ALTER INDEX IF EXISTS cuenta_bolsillo_pkey RENAME TO origen_fondos_pkey;
ALTER INDEX IF EXISTS idx_cuenta_bolsillo_metodo_pago RENAME TO idx_origen_fondos_metodo_pago;
ALTER INDEX IF EXISTS idx_cuenta_bolsillo_parent RENAME TO idx_origen_fondos_parent;
ALTER INDEX IF EXISTS idx_cuenta_bolsillo_tipo RENAME TO idx_origen_fondos_tipo;

ALTER TABLE origen_fondos
    RENAME CONSTRAINT cuenta_bolsillo_metodo_pago_id_fkey TO origen_fondos_metodo_pago_id_fkey;
ALTER TABLE origen_fondos
    RENAME CONSTRAINT cuenta_bolsillo_parent_cuenta_bolsillo_id_fkey TO origen_fondos_parent_origen_fondos_id_fkey;
ALTER TABLE origen_fondos
    RENAME CONSTRAINT cuenta_bolsillo_proveedor_id_fkey TO origen_fondos_proveedor_id_fkey;
ALTER TABLE origen_fondos
    RENAME CONSTRAINT cuenta_bolsillo_tipo_bolsillo_id_fkey TO origen_fondos_tipo_origen_fondos_id_fkey;

ALTER SEQUENCE IF EXISTS cuenta_bolsillo_id_seq RENAME TO origen_fondos_id_seq;

-- 3) Movimientos
ALTER TABLE IF EXISTS movimiento_bolsillo RENAME TO movimiento_origen_fondos;
ALTER TABLE movimiento_origen_fondos RENAME COLUMN cuenta_bolsillo_id TO origen_fondos_id;
ALTER TABLE movimiento_origen_fondos RENAME COLUMN cuenta_destino_id TO origen_destino_id;

ALTER INDEX IF EXISTS movimiento_bolsillo_pkey RENAME TO movimiento_origen_fondos_pkey;
ALTER INDEX IF EXISTS idx_movimiento_cuenta RENAME TO idx_movimiento_origen_fondos;
ALTER INDEX IF EXISTS idx_movimiento_fecha RENAME TO idx_movimiento_origen_fondos_fecha;
ALTER INDEX IF EXISTS idx_movimiento_grupo_traslado RENAME TO idx_movimiento_origen_fondos_grupo;

ALTER TABLE movimiento_origen_fondos
    RENAME CONSTRAINT movimiento_bolsillo_cuenta_bolsillo_id_fkey TO movimiento_origen_fondos_origen_fondos_id_fkey;
ALTER TABLE movimiento_origen_fondos
    RENAME CONSTRAINT movimiento_bolsillo_cuenta_destino_id_fkey TO movimiento_origen_fondos_origen_destino_id_fkey;
ALTER TABLE movimiento_origen_fondos
    RENAME CONSTRAINT movimiento_bolsillo_metodo_pago_id_fkey TO movimiento_origen_fondos_metodo_pago_id_fkey;
ALTER TABLE movimiento_origen_fondos
    RENAME CONSTRAINT movimiento_bolsillo_motivo_movimiento_id_fkey TO movimiento_origen_fondos_motivo_movimiento_id_fkey;
ALTER TABLE movimiento_origen_fondos
    RENAME CONSTRAINT chk_movimiento_traslado_destino TO chk_movimiento_origen_traslado_destino;

ALTER SEQUENCE IF EXISTS movimiento_bolsillo_id_seq RENAME TO movimiento_origen_fondos_id_seq;

-- 4) Egreso
ALTER TABLE egreso RENAME COLUMN cuenta_bolsillo_id TO origen_fondos_id;
ALTER INDEX IF EXISTS idx_egreso_cuenta_bolsillo RENAME TO idx_egreso_origen_fondos;
ALTER TABLE egreso
    RENAME CONSTRAINT egreso_cuenta_bolsillo_id_fkey TO egreso_origen_fondos_id_fkey;
