-- Numeración interna POS (VTA, NC, ND, movimientos inventario)
-- Ejecutar después de 00_establecimiento.sql

CREATE TABLE IF NOT EXISTS consecutivo_documento (
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(30) NOT NULL,
    anio            INTEGER NOT NULL,
    ultimo_numero   BIGINT NOT NULL DEFAULT 0,
    prefijo         VARCHAR(10) NOT NULL,
    CONSTRAINT uq_consecutivo_tipo_anio UNIQUE (tipo, anio)
);

INSERT INTO consecutivo_documento (tipo, anio, ultimo_numero, prefijo)
VALUES
    ('VENTA', EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER, 0, 'VTA'),
    ('NC', EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER, 0, 'NC'),
    ('ND', EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER, 0, 'ND'),
    ('MOV_INVENTARIO', EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER, 0, 'MINV')
ON CONFLICT (tipo, anio) DO NOTHING;
