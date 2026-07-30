-- Watermark de ledger para cierre de turno (espejo de ultimo_historial_recibo_id).
-- Periodo de "Movimientos" del siguiente corte: id > ultimo_movimiento_origen_fondos_id.
-- Incluye AJUSTE_CIERRE del propio corte (origen_tipo=CIERRE, origen_id=corte.id).

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS ultimo_movimiento_origen_fondos_id BIGINT;

COMMENT ON COLUMN corte_venta.ultimo_movimiento_origen_fondos_id IS
    'Último movimiento_origen_fondos.id incluido en este corte (incluye AJUSTE_CIERRE). El siguiente turno cuenta id > este valor.';

CREATE INDEX IF NOT EXISTS idx_corte_venta_ultimo_mov
    ON corte_venta (ultimo_movimiento_origen_fondos_id);

-- Backfill: max(movimientos hasta fecha_creacion del corte, AJUSTE_CIERRE del corte)
UPDATE corte_venta cv
SET ultimo_movimiento_origen_fondos_id = NULLIF(GREATEST(
    COALESCE((
        SELECT MAX(m.id) FROM movimiento_origen_fondos m
        WHERE m.fecha_creacion <= cv.fecha_creacion
    ), 0),
    COALESCE((
        SELECT MAX(m.id) FROM movimiento_origen_fondos m
        WHERE m.origen_tipo = 'CIERRE' AND m.origen_id = cv.id
    ), 0)
), 0)
WHERE cv.estado <> 'eliminado';
