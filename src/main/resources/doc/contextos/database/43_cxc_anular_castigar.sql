-- 43 — CxC: anular (0 abonos) + castigar cartera (salida inventario a costo)
-- No usa nota_ajuste_documento (eso es NC/ND sobre documento_venta / DIAN).
-- No genera movimiento OF: castigo ≠ egreso de caja.

-- 1) Estado CASTIGADA
ALTER TABLE cuenta_por_cobrar
    DROP CONSTRAINT IF EXISTS ck_cxc_estado;

ALTER TABLE cuenta_por_cobrar
    ADD CONSTRAINT ck_cxc_estado
    CHECK (estado IN ('ABIERTA', 'PARCIAL', 'PAGADA', 'ANULADA', 'CASTIGADA'));

-- 2) Traza de cierre (anulación o castigo)
ALTER TABLE cuenta_por_cobrar
    ADD COLUMN IF NOT EXISTS fecha_cierre TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS usuario_cierre_id UUID,
    ADD COLUMN IF NOT EXISTS motivo_operacion_id BIGINT,
    ADD COLUMN IF NOT EXISTS motivo_cierre_texto TEXT,
    ADD COLUMN IF NOT EXISTS movimiento_inventario_id BIGINT,
    ADD COLUMN IF NOT EXISTS valor_perdida_costo NUMERIC(12, 2);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cxc_motivo_cierre'
    ) THEN
        ALTER TABLE cuenta_por_cobrar
            ADD CONSTRAINT fk_cxc_motivo_cierre
            FOREIGN KEY (motivo_operacion_id) REFERENCES motivo_operacion (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cxc_movimiento_inventario'
    ) THEN
        ALTER TABLE cuenta_por_cobrar
            ADD CONSTRAINT fk_cxc_movimiento_inventario
            FOREIGN KEY (movimiento_inventario_id) REFERENCES movimiento_inventario (id);
    END IF;
END $$;

COMMENT ON COLUMN cuenta_por_cobrar.fecha_cierre IS
    'Anulación o castigo: cuándo se cerró la CxC vigente.';
COMMENT ON COLUMN cuenta_por_cobrar.valor_perdida_costo IS
    'Solo CASTIGADA: Σ costo unitario × cantidad (fallback ~85% PV). No es egreso de caja.';

-- 3) Motivos
INSERT INTO motivo_operacion (codigo, nombre, aplica_a)
VALUES
    ('CXC_ANULAR_SIN_ABONOS', 'Anular crédito sin abonos (cliente paga de contado / error)', 'CXC'),
    ('CXC_CASTIGO_CARTERA', 'Castigo de cartera (irrecuperable)', 'CXC')
ON CONFLICT (codigo) DO NOTHING;

-- 4) Tipo inventario (familia merma, semántica propia)
INSERT INTO tipo_movimiento_inventario (codigo, nombre, direccion)
VALUES
    ('CASTIGO_CARTERA', 'Castigo de cartera / crédito irrecuperable', 'SALIDA')
ON CONFLICT (codigo) DO NOTHING;
