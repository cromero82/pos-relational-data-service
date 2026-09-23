-- Sprint B1 — Catálogos tipo bolsillo y motivo movimiento

CREATE TABLE IF NOT EXISTS tipo_bolsillo (
    id          SERIAL PRIMARY KEY,
    codigo      VARCHAR(40) NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS motivo_movimiento (
    id          SERIAL PRIMARY KEY,
    codigo      VARCHAR(50) NOT NULL UNIQUE,
    nombre      VARCHAR(150) NOT NULL,
    categoria   VARCHAR(30) NOT NULL DEFAULT 'AJUSTE',
    sistema     BOOLEAN NOT NULL DEFAULT FALSE,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    orden       INTEGER NOT NULL DEFAULT 0
);

INSERT INTO tipo_bolsillo (codigo, nombre, descripcion)
VALUES
    ('OPERATIVO', 'Operativo del día', 'Caja y medios del turno'),
    ('RESERVA_PROVEEDORES', 'Reserva proveedores', 'Reserva surtir / pago proveedores'),
    ('FACTURA_PROVEEDOR', 'Factura proveedor', 'Pago factura proveedor específico'),
    ('ARRIENDO', 'Arriendo', 'Reserva arriendo'),
    ('PRESTAMO', 'Préstamo', 'Dinero recibido en préstamo'),
    ('TARJETA_CREDITO', 'Tarjeta crédito', 'Línea tarjeta crédito empresa'),
    ('AHORRO', 'Ahorro / nómina', 'Cuenta ahorro o nómina'),
    ('OTRO', 'Otro', 'Otro bolsillo')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO motivo_movimiento (codigo, nombre, categoria, sistema, orden)
VALUES
    ('PENDIENTE_EXTRACTO', 'Pendiente ajustar en extracto del día', 'AJUSTE', TRUE, 10),
    ('COMPRA_PERSONAL', 'Compras personales / no actividad del negocio', 'AJUSTE', TRUE, 20),
    ('PAGO_PAREJA_TC', 'Movimiento tarjeta débito personal (pareja)', 'AJUSTE', TRUE, 30),
    ('MAS_PROVEEDORES', 'Más proveedores de lo planeado', 'BOLSILLO', TRUE, 40),
    ('INSUMO_NO_PROGRAMADO', 'Insumos o servicios no programados', 'BOLSILLO', TRUE, 50),
    ('FALLA_SISTEMA_BANCO', 'Posible falla sistema / banco en mantenimiento', 'AJUSTE', TRUE, 60),
    ('AJUSTE_PERSONAL_FALTANTE', 'Ajuste con dinero personal del administrador', 'AJUSTE', TRUE, 70),
    ('OTRO', 'Otro', 'AJUSTE', TRUE, 99)
ON CONFLICT (codigo) DO NOTHING;
