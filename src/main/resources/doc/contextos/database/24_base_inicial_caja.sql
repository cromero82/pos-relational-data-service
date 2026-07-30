-- Inversión inicial / Base del primer turno (instalación sin cortes).

INSERT INTO motivo_movimiento (codigo, nombre, categoria, sistema, orden)
VALUES (
    'INVERSION_INICIAL_BASE',
    'Inversion inicial correspondiente a: BASE para caja registradora',
    'BASE_TURNO',
    TRUE,
    5
)
ON CONFLICT (codigo) DO UPDATE
SET nombre = EXCLUDED.nombre,
    categoria = EXCLUDED.categoria,
    sistema = EXCLUDED.sistema,
    activo = TRUE,
    orden = EXCLUDED.orden;

COMMENT ON TABLE motivo_movimiento IS
    'Motivos de movimiento; categoria BASE_TURNO solo para escenarios de sistema (instalación / distribución).';
