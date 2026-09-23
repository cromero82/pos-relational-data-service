-- Distribución de apertura post-migrate: el Contado de efectivo del último corte
-- legacy (prod no tenía Distribución ni ledger OF) se siembra en Caja: Efectivo
-- y se parte como si se hubiera distribuido:
--   base  = corte-venta.base-efectivo (default 150000)
--   resto → Caja Menor
-- Marca ese corte: base_siguiente_efectivo + distribucion CONFIRMADA.
-- Idempotente. No ejecutar sobre controlneg_rmx_db (dev) salvo URL explícita.

DO $$
DECLARE
    v_corte_id BIGINT;
    v_usuario VARCHAR(36);
    v_contado NUMERIC(14, 2);
    v_base NUMERIC(14, 2);
    v_a_menor NUMERIC(14, 2);
    v_caja INTEGER;
    v_menor INTEGER;
    v_mp_efectivo BIGINT;
    v_saldo_caja NUMERIC(14, 2);
    v_saldo_menor NUMERIC(14, 2);
    v_grupo VARCHAR(36);
BEGIN
    IF EXISTS (
        SELECT 1 FROM movimiento_origen_fondos
        WHERE origen_tipo = 'MIGRACION_CONTADO_LEGACY'
    ) THEN
        RAISE NOTICE '64_: ya existe MIGRACION_CONTADO_LEGACY; skip';
        RETURN;
    END IF;

    SELECT of1.id, of2.id
    INTO v_caja, v_menor
    FROM origen_fondos of1
    CROSS JOIN origen_fondos of2
    WHERE of1.nombre ILIKE 'Caja: Efectivo'
      AND of2.nombre ILIKE 'Caja Menor'
    LIMIT 1;

    IF v_caja IS NULL OR v_menor IS NULL THEN
        RAISE EXCEPTION '64_: faltan OF Caja: Efectivo o Caja Menor';
    END IF;

    SELECT COALESCE(of.metodo_pago_id, 1)
    INTO v_mp_efectivo
    FROM origen_fondos of
    WHERE of.id = v_caja;

    SELECT COALESCE(
        (SELECT NULLIF(btrim(value), '')::NUMERIC FROM configuracion_app WHERE key = 'corte-venta.base-efectivo'),
        150000
    )
    INTO v_base;

    SELECT cv.id, cv.usuario_id, d.total
    INTO v_corte_id, v_usuario, v_contado
    FROM corte_venta cv
    JOIN corte_venta_detalle d
      ON d.corte_venta_id = cv.id
     AND d.metodo_pago_id = v_mp_efectivo
    WHERE cv.estado <> 'eliminado'
      AND cv.base_siguiente_efectivo IS NULL
    ORDER BY cv.id DESC
    LIMIT 1;

    IF v_corte_id IS NULL OR v_contado IS NULL OR v_contado <= 0 THEN
        RAISE NOTICE '64_: no hay corte legacy con Contado efectivo; skip';
        RETURN;
    END IF;

    IF v_base < 0 THEN
        v_base := 0;
    END IF;
    IF v_base > v_contado THEN
        v_base := v_contado;
    END IF;
    v_a_menor := v_contado - v_base;

    SELECT COALESCE(SUM(impacto), 0) INTO v_saldo_caja
    FROM movimiento_origen_fondos WHERE origen_fondos_id = v_caja;
    SELECT COALESCE(SUM(impacto), 0) INTO v_saldo_menor
    FROM movimiento_origen_fondos WHERE origen_fondos_id = v_menor;

    INSERT INTO movimiento_origen_fondos (
        fecha, usuario_id, origen_fondos_id, tipo_movimiento,
        valor, impacto, saldo_antes, saldo_despues,
        metodo_pago_id, observacion, origen_tipo, id_referencia
    ) VALUES (
        CURRENT_DATE,
        COALESCE(v_usuario, '00000000-0000-0000-0000-000000000000'),
        v_caja,
        'ENTRADA_MANUAL',
        v_contado,
        v_contado,
        v_saldo_caja,
        v_saldo_caja + v_contado,
        v_mp_efectivo,
        'Migrate: Contado efectivo corte #' || v_corte_id || ' (prod sin distribución)',
        'MIGRACION_CONTADO_LEGACY',
        v_corte_id
    );
    v_saldo_caja := v_saldo_caja + v_contado;

    IF v_a_menor > 0 THEN
        v_grupo := gen_random_uuid()::TEXT;
        INSERT INTO movimiento_origen_fondos (
            fecha, usuario_id, origen_fondos_id, origen_destino_id, tipo_movimiento,
            valor, impacto, saldo_antes, saldo_despues,
            metodo_pago_id, observacion, origen_tipo, id_referencia, grupo_traslado_id
        ) VALUES (
            CURRENT_DATE,
            COALESCE(v_usuario, '00000000-0000-0000-0000-000000000000'),
            v_caja,
            v_menor,
            'TRASLADO',
            v_a_menor,
            -v_a_menor,
            v_saldo_caja,
            v_saldo_caja - v_a_menor,
            v_mp_efectivo,
            'Migrate: distribución Contado corte #' || v_corte_id || ' → Caja Menor',
            'DISTRIBUCION',
            v_corte_id,
            v_grupo
        );
        INSERT INTO movimiento_origen_fondos (
            fecha, usuario_id, origen_fondos_id, tipo_movimiento,
            valor, impacto, saldo_antes, saldo_despues,
            observacion, origen_tipo, id_referencia, grupo_traslado_id
        ) VALUES (
            CURRENT_DATE,
            COALESCE(v_usuario, '00000000-0000-0000-0000-000000000000'),
            v_menor,
            'TRASLADO',
            v_a_menor,
            v_a_menor,
            v_saldo_menor,
            v_saldo_menor + v_a_menor,
            'Migrate: distribución Contado corte #' || v_corte_id || ' → Caja Menor',
            'DISTRIBUCION',
            v_corte_id,
            v_grupo
        );
    END IF;

    UPDATE corte_venta
    SET base_siguiente_efectivo = v_base,
        distribucion_efectivo_estado = 'CONFIRMADA'
    WHERE id = v_corte_id;

    RAISE NOTICE '64_: corte #%, Contado %, base %, Caja Menor %',
        v_corte_id, v_contado, v_base, v_a_menor;
END $$;
