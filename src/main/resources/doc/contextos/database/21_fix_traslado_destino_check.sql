-- Hotfix: el traslado genera DOS filas TRASLADO
-- 1) salida (impacto < 0): origen_destino_id = cuenta destino (obligatorio)
-- 2) entrada (impacto > 0): origen_fondos_id = cuenta destino; origen_destino_id NULL
-- El check original exigía destino en AMBAS y bloqueaba el traslado.

ALTER TABLE movimiento_origen_fondos
    DROP CONSTRAINT IF EXISTS chk_movimiento_origen_traslado_destino;

ALTER TABLE movimiento_origen_fondos
    ADD CONSTRAINT chk_movimiento_origen_traslado_destino
    CHECK (
        tipo_movimiento <> 'TRASLADO'
        OR impacto >= 0
        OR origen_destino_id IS NOT NULL
    );

COMMENT ON CONSTRAINT chk_movimiento_origen_traslado_destino ON movimiento_origen_fondos IS
    'En TRASLADO, solo la pata de salida (impacto < 0) exige origen_destino_id.';
