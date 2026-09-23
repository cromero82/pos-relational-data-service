-- Backfill ventas históricas pagadas → documento_venta VTA-######
-- Mismo formato que ConsecutivoDocumentoServiceImpl.nextConsecutivo(VENTA):
--   prefijo + "-" + 6 dígitos (ej. VTA-000001).
-- Ejecutar UNA vez después de 05_documento_venta.sql
-- Idempotente: omite historial_recibo que ya tienen documento_venta_id
-- Si un ensayo previo dejó VTA-LEGACY-*, 62_normalize_documento_venta_vta.sql los renombra.

DO $$
DECLARE
    r RECORD;
    v_anio INTEGER := EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER;
    v_num BIGINT;
    v_consec VARCHAR(30);
    v_doc_id BIGINT;
BEGIN
    SELECT COALESCE(MAX(
        CASE
            WHEN consecutivo ~ '^VTA-([0-9]+)$'
            THEN (regexp_match(consecutivo, '^VTA-([0-9]+)$'))[1]::BIGINT
            WHEN consecutivo ~ '^VTA-LEGACY-([0-9]+)$'
            THEN (regexp_match(consecutivo, '^VTA-LEGACY-([0-9]+)$'))[1]::BIGINT
            ELSE 0
        END
    ), 0)
    INTO v_num
    FROM documento_venta
    WHERE consecutivo ~ '^VTA-([0-9]+)$'
       OR consecutivo ~ '^VTA-LEGACY-([0-9]+)$';

    FOR r IN
        SELECT hr.id, hr.fecha_creacion, hr.total, hr.metodo_pago_id, hr.cliente_id, hr.sesion_id
        FROM historial_recibo hr
        WHERE hr.estado_id = 2
          AND hr.documento_venta_id IS NULL
          AND NOT EXISTS (SELECT 1 FROM documento_venta dv WHERE dv.historial_recibo_id = hr.id)
        ORDER BY hr.id
    LOOP
        v_num := v_num + 1;
        v_consec := 'VTA-' || LPAD(v_num::TEXT, 6, '0');

        INSERT INTO documento_venta (
            consecutivo, anio, historial_recibo_id, fecha_hecho, total,
            metodo_pago_id, cliente_id, sesion_id, estado
        )
        VALUES (
            v_consec,
            COALESCE(EXTRACT(YEAR FROM r.fecha_creacion)::INTEGER, v_anio),
            r.id,
            r.fecha_creacion,
            r.total,
            r.metodo_pago_id,
            r.cliente_id,
            r.sesion_id,
            'VIGENTE'
        )
        RETURNING id INTO v_doc_id;

        UPDATE historial_recibo SET documento_venta_id = v_doc_id WHERE id = r.id;
    END LOOP;

    UPDATE consecutivo_documento
    SET ultimo_numero = GREATEST(ultimo_numero, v_num)
    WHERE tipo = 'VENTA' AND anio = v_anio;
END $$;
