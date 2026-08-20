-- 35 — Motivos desfase con acción esperada + MOVIMIENTO_NO_REGISTRADO

ALTER TABLE motivo_movimiento
    ADD COLUMN IF NOT EXISTS accion_esperada VARCHAR(40);

COMMENT ON COLUMN motivo_movimiento.accion_esperada IS
    'HINT operativo: TRASLADO_OF | REGISTRAR_DOCUMENTO | AJUSTE_CIERRE | REVISAR';

INSERT INTO motivo_movimiento (codigo, nombre, categoria, sistema, orden, accion_esperada)
VALUES
    ('MOVIMIENTO_NO_REGISTRADO',
     'Movimiento o egreso no registrado en el sistema',
     'DESFASE_CIERRE', TRUE, 25, 'REGISTRAR_DOCUMENTO')
ON CONFLICT (codigo) DO UPDATE
SET accion_esperada = EXCLUDED.accion_esperada,
    nombre = EXCLUDED.nombre;

UPDATE motivo_movimiento SET accion_esperada = 'TRASLADO_OF'
WHERE codigo = 'ERROR_MEDIO_PAGO' AND (accion_esperada IS NULL OR accion_esperada = '');

UPDATE motivo_movimiento SET accion_esperada = 'AJUSTE_CIERRE'
WHERE codigo IN ('ERROR_CONTEO', 'FALTA_CAMBIO')
  AND (accion_esperada IS NULL OR accion_esperada = '');

UPDATE motivo_movimiento SET accion_esperada = 'REGISTRAR_DOCUMENTO'
WHERE codigo = 'AJUSTE_PERSONAL_CIERRE'
  AND (accion_esperada IS NULL OR accion_esperada = '');

UPDATE motivo_movimiento SET accion_esperada = 'REVISAR'
WHERE codigo IN ('DESCONOCIDO', 'OTRO_DESFASE')
  AND (accion_esperada IS NULL OR accion_esperada = '');

UPDATE motivo_movimiento SET accion_esperada = 'REGISTRAR_DOCUMENTO'
WHERE codigo = 'MOVIMIENTO_NO_REGISTRADO';
