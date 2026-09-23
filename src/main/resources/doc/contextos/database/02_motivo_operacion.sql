-- Catálogo de motivos (anulación, restauración, inventario)
-- Sprint 2 usará estos códigos; se crean en Sprint 0 para integridad referencial futura.

CREATE TABLE IF NOT EXISTS motivo_operacion (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(40) NOT NULL UNIQUE,
    nombre      VARCHAR(150) NOT NULL,
    aplica_a    VARCHAR(40) NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO motivo_operacion (codigo, nombre, aplica_a)
VALUES
    ('ERROR_PAGO', 'Pago registrado por error', 'RESTAURACION'),
    ('RESTAURACION_TICKET', 'Reintegro / restauración de ticket', 'RESTAURACION'),
    ('REAPERTURA_PENDIENTE', 'Reapertura a pendiente de pago', 'VENTA'),
    ('DEVOLUCION_CLIENTE', 'Devolución al cliente', 'NC'),
    ('CORRECCION_PRECIO', 'Corrección de precio o cantidad', 'NC'),
    ('ANULACION_ADMIN', 'Anulación administrativa', 'NC')
ON CONFLICT (codigo) DO NOTHING;
