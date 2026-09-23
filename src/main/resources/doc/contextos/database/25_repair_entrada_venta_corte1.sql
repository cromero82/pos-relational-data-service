-- Reparación del corte #1: ventas no contabilizadas en ledger antes de distribución.
-- Estado actual erróneo: Caja 60k + Menor 40k (distribuyó sobre 100k en vez de 250k).
-- Objetivo: Base 60k + Menor 40k + General 150k = 250k.

DO $$
DECLARE
    v_usuario VARCHAR(36);
    v_saldo_caja NUMERIC(14,2);
    v_saldo_gen NUMERIC(14,2);
    v_fecha DATE := CURRENT_DATE;
BEGIN
    SELECT usuario_id INTO v_usuario
    FROM movimiento_origen_fondos
    ORDER BY id
    LIMIT 1;

    IF v_usuario IS NULL THEN
        RAISE EXCEPTION 'No hay movimientos para tomar usuario_id';
    END IF;

    -- 1) Contabilizar ventas del corte si faltan
    IF NOT EXISTS (
        SELECT 1 FROM movimiento_origen_fondos
        WHERE origen_tipo = 'CORTE_VENTA' AND origen_id = 1
    ) THEN
        SELECT COALESCE(SUM(impacto), 0) INTO v_saldo_caja
        FROM movimiento_origen_fondos WHERE origen_fondos_id = 1;

        INSERT INTO movimiento_origen_fondos (
            fecha, fecha_creacion, usuario_id, origen_fondos_id, tipo_movimiento,
            valor, impacto, saldo_antes, saldo_despues, metodo_pago_id,
            observacion, valor_sistema, origen_tipo, origen_id
        ) VALUES (
            v_fecha, NOW(), v_usuario, 1, 'ENTRADA_VENTA',
            150000, 150000, v_saldo_caja, v_saldo_caja + 150000, 1,
            'Ventas del corte #1 (reparación)', 150000, 'CORTE_VENTA', 1
        );
    END IF;

    -- 2) Trasladar el faltante a Caja General (150k) si aún no está
    IF NOT EXISTS (
        SELECT 1 FROM movimiento_origen_fondos
        WHERE origen_tipo = 'DISTRIBUCION' AND origen_id = 1
          AND origen_fondos_id = 1 AND origen_destino_id = 5
          AND valor = 150000
    ) THEN
        SELECT COALESCE(SUM(impacto), 0) INTO v_saldo_caja
        FROM movimiento_origen_fondos WHERE origen_fondos_id = 1;
        SELECT COALESCE(SUM(impacto), 0) INTO v_saldo_gen
        FROM movimiento_origen_fondos WHERE origen_fondos_id = 5;

        IF v_saldo_caja < 150000 THEN
            RAISE EXCEPTION 'Saldo Caja insuficiente para traslado a General: %', v_saldo_caja;
        END IF;

        INSERT INTO movimiento_origen_fondos (
            fecha, fecha_creacion, usuario_id, origen_fondos_id, origen_destino_id,
            tipo_movimiento, valor, impacto, saldo_antes, saldo_despues, metodo_pago_id,
            observacion, origen_tipo, origen_id, grupo_traslado_id
        ) VALUES (
            v_fecha, NOW(), v_usuario, 1, 5,
            'TRASLADO', 150000, -150000, v_saldo_caja, v_saldo_caja - 150000, 1,
            'Distribución de efectivo corte #1 (reparación General)',
            'DISTRIBUCION', 1, gen_random_uuid()::text
        );

        INSERT INTO movimiento_origen_fondos (
            fecha, fecha_creacion, usuario_id, origen_fondos_id,
            tipo_movimiento, valor, impacto, saldo_antes, saldo_despues, metodo_pago_id,
            observacion, origen_tipo, origen_id, grupo_traslado_id
        )
        SELECT
            v_fecha, NOW(), v_usuario, 5,
            'TRASLADO', 150000, 150000, v_saldo_gen, v_saldo_gen + 150000, NULL,
            'Distribución de efectivo corte #1 (reparación General)',
            'DISTRIBUCION', 1, m.grupo_traslado_id
        FROM movimiento_origen_fondos m
        WHERE m.origen_tipo = 'DISTRIBUCION' AND m.origen_id = 1
          AND m.origen_fondos_id = 1 AND m.origen_destino_id = 5
          AND m.valor = 150000
        ORDER BY m.id DESC
        LIMIT 1;
    END IF;

    UPDATE corte_venta
    SET base_siguiente_efectivo = 60000,
        distribucion_efectivo_estado = 'CONFIRMADA',
        ultimo_movimiento_origen_fondos_id = (SELECT MAX(id) FROM movimiento_origen_fondos)
    WHERE id = 1;
END $$;

-- Verificación
SELECT origen_fondos_id, SUM(impacto) AS saldo
FROM movimiento_origen_fondos
GROUP BY origen_fondos_id
ORDER BY origen_fondos_id;

SELECT id, tipo_movimiento, origen_tipo, valor, impacto, saldo_despues, origen_fondos_id, origen_destino_id
FROM movimiento_origen_fondos
ORDER BY id;
