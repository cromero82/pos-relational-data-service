-- Sprint 4 — Arqueo: desglose ventas/egresos por medio en corte

ALTER TABLE ventas_tipo
    ADD COLUMN IF NOT EXISTS total_ventas_sistema NUMERIC(12, 2) NULL;

ALTER TABLE ventas_tipo
    ADD COLUMN IF NOT EXISTS total_egresos_sistema NUMERIC(12, 2) NULL;

ALTER TABLE ventas_tipo
    ADD COLUMN IF NOT EXISTS desfase NUMERIC(12, 2) NULL;

UPDATE ventas_tipo
SET total_ventas_sistema = total_sistema,
    total_egresos_sistema = 0
WHERE total_ventas_sistema IS NULL AND total_sistema IS NOT NULL;

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS sesion_id BIGINT NULL;

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS fondo_inicial_efectivo NUMERIC(12, 2) NULL;

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS motivo_desfase TEXT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_corte_venta_sesion'
    ) THEN
        ALTER TABLE corte_venta
            ADD CONSTRAINT fk_corte_venta_sesion
            FOREIGN KEY (sesion_id) REFERENCES sesion (id);
    END IF;
END $$;
