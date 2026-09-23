-- Distribución de efectivo post-cierre (Base del siguiente turno).

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS distribucion_efectivo_estado VARCHAR(20);

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS base_siguiente_efectivo NUMERIC(14, 2);

COMMENT ON COLUMN corte_venta.distribucion_efectivo_estado IS
    'PENDIENTE | CONFIRMADA. Null en cortes legacy sin flujo de distribución.';

COMMENT ON COLUMN corte_venta.base_siguiente_efectivo IS
    'Saldo que queda en Caja: Efectivo tras distribución (= Base del próximo turno).';

-- Cortes vigentes recientes: marcar pendientes si aún no tienen estado
UPDATE corte_venta
SET distribucion_efectivo_estado = 'PENDIENTE'
WHERE estado <> 'eliminado'
  AND distribucion_efectivo_estado IS NULL
  AND id = (
      SELECT MAX(id) FROM corte_venta WHERE estado <> 'eliminado'
  );
