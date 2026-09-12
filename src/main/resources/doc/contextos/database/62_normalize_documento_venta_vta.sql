-- Normaliza documentos de venta migrados: VTA-LEGACY-###### → VTA-######
-- Mismo formato que ConsecutivoDocumentoServiceImpl.nextConsecutivo(VENTA).
-- Idempotente. Ensayo v02 (ya backfilleado con prefijo LEGACY) y prod v02 final.
-- No toca VTA- emitidos en runtime. No ejecutar sobre controlneg_rmx_db (dev laptop).

UPDATE documento_venta
SET consecutivo = regexp_replace(consecutivo, '^VTA-LEGACY-', 'VTA-')
WHERE consecutivo ~ '^VTA-LEGACY-[0-9]+$';

UPDATE consecutivo_documento cd
SET ultimo_numero = GREATEST(
        cd.ultimo_numero,
        COALESCE((
            SELECT MAX((regexp_match(dv.consecutivo, '^VTA-([0-9]+)$'))[1]::BIGINT)
            FROM documento_venta dv
            WHERE dv.anio = cd.anio
              AND dv.consecutivo ~ '^VTA-[0-9]+$'
        ), 0)
    )
WHERE cd.tipo = 'VENTA';
