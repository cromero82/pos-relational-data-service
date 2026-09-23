-- Fuente de verdad de Ingresos: ventas POS del corte (tickets), no Contado ni Esperado.
-- No aplicar en controlneg_rmx_db (dev) ni sandbox; sí en controlneg_rmx_db_v02 / prod v02.

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS total_ventas_sistema NUMERIC(14, 2) NOT NULL DEFAULT 0;

COMMENT ON COLUMN corte_venta.total_ventas_sistema IS
    'Σ tickets cobrados del corte (totalVentasSistema). Dashboard Ingresos. Distinto de total (Contado) y total_sistema (Esperado).';

UPDATE corte_venta c
SET total_ventas_sistema = COALESCE((
        SELECT SUM(d.total_ventas_sistema)
        FROM corte_venta_detalle d
        WHERE d.corte_venta_id = c.id
    ), (
        SELECT SUM(vt.total_ventas_sistema)
        FROM ventas_tipo vt
        WHERE vt.corte_venta_id = c.id
    ), 0);
