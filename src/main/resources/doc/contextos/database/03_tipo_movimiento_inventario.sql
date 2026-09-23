-- Tipos de movimiento inventario (Sprint 0 / kardex Sprint 3)
-- Ejecutar después de 02_motivo_operacion.sql

CREATE TABLE IF NOT EXISTS tipo_movimiento_inventario (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(40) NOT NULL UNIQUE,
    nombre      VARCHAR(150) NOT NULL,
    direccion   VARCHAR(10) NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_tmi_direccion CHECK (direccion IN ('ENTRADA', 'SALIDA', 'AJUSTE'))
);

INSERT INTO tipo_movimiento_inventario (codigo, nombre, direccion)
VALUES
    ('COMPRA_EGRESO', 'Compra proveedor (entrada almacén)', 'ENTRADA'),
    ('VENTA_POS', 'Venta en POS', 'SALIDA'),
    ('REINTEGRO_VENTA', 'Reintegro por anulación o restauración', 'ENTRADA'),
    ('AJUSTE_CONTEO', 'Ajuste por conteo físico', 'AJUSTE'),
    ('MERMA_VENCIDO', 'Producto vencido', 'SALIDA'),
    ('MERMA_SIN_RECAMBIO', 'Merma sin recambio', 'SALIDA'),
    ('DONACION', 'Donación recibida', 'ENTRADA'),
    ('PROMO_PROVEEDOR', 'Promoción de proveedor', 'ENTRADA')
ON CONFLICT (codigo) DO NOTHING;
