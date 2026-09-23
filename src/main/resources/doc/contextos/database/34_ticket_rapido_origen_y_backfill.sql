-- 34 — Ticket rápido: flag + asegurar pago + documento (backfill)
-- Idempotente. createQuick (BE) escribe estos campos en ventas nuevas.

ALTER TABLE historial_recibo
    ADD COLUMN IF NOT EXISTS ticket_rapido BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN historial_recibo.ticket_rapido IS
    'TRUE si se creó vía Ticket rápido (VARIOSPROD, sin ítems reales). '
    'Ocultar/bloquear cuando establecimiento es RESPONSABLE_IVA.';

-- Marcar históricos: un solo detalle al producto VARIOSPROD
UPDATE historial_recibo h
SET ticket_rapido = TRUE
WHERE h.ticket_rapido = FALSE
  AND EXISTS (
      SELECT 1
      FROM historial_recibo_detalle d
      JOIN producto p ON p.id = d.producto_id
      WHERE d.recibo_id = h.id
        AND p.codigo_barras = 'VARIOSPROD'
  )
  AND (
      SELECT COUNT(*) FROM historial_recibo_detalle d2 WHERE d2.recibo_id = h.id
  ) = 1;

-- Backfill líneas de pago faltantes (misma lógica que 30_, por si quedó hueco)
INSERT INTO historial_recibo_pago (historial_recibo_id, metodo_pago_id, monto, orden)
SELECT
    h.id,
    h.metodo_pago_id,
    h.total,
    1
FROM historial_recibo h
WHERE h.metodo_pago_id IS NOT NULL
  AND h.total IS NOT NULL
  AND h.total > 0
  AND NOT EXISTS (
      SELECT 1 FROM historial_recibo_pago p WHERE p.historial_recibo_id = h.id
  );

-- Nota: documento_venta para quick históricos se genera on-read / en BE al consultar;
-- el backfill masivo de consecutivos ya está en 05_backfill_documento_venta.sql.
-- Si un quick quedó sin documento, re-ejecutar 05 o crear al abrir historial.
