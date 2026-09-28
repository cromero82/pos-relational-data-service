-- Copia de los indicadores del Cierre de turno al momento de registrar el corte.
-- La vista de solo lectura (Ingresos → Ver) no recalcula contra el ledger actual.

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS kpi_ventas_turno NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS kpi_efectivo_disponible NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS kpi_medios_electronicos NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS kpi_total_disponible NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS kpi_cartera NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS kpi_cartera_cobrada NUMERIC(14, 2);

COMMENT ON COLUMN corte_venta.kpi_ventas_turno IS
    'Snapshot: ventas de tickets + cobranzas CxC al registrar el corte.';
COMMENT ON COLUMN corte_venta.kpi_efectivo_disponible IS
    'Snapshot: efectivo (Real de filas físicas + otras cajas) al registrar.';
COMMENT ON COLUMN corte_venta.kpi_medios_electronicos IS
    'Snapshot: dinero en medios no físicos al registrar.';
COMMENT ON COLUMN corte_venta.kpi_total_disponible IS
    'Snapshot: efectivo + medios electrónicos al registrar.';
COMMENT ON COLUMN corte_venta.kpi_cartera IS
    'Snapshot: saldo CxC vigente (ABIERTA/PARCIAL) al registrar.';
COMMENT ON COLUMN corte_venta.kpi_cartera_cobrada IS
    'Snapshot: abonos CxC (ENTRADA_COBRANZA) del turno al registrar.';
