-- Pendientes electrónicos también para abonos CxC (sin historial_recibo aún).
-- Origen XOR: historial_recibo_id XOR abono_cxc_id.

ALTER TABLE historial_recibos_electronicos
    ALTER COLUMN historial_recibo_id DROP NOT NULL;

ALTER TABLE historial_recibos_electronicos
    ADD COLUMN IF NOT EXISTS abono_cxc_id BIGINT REFERENCES abono_cxc (id);

ALTER TABLE historial_recibos_electronicos
    DROP CONSTRAINT IF EXISTS uq_hre_historial;

CREATE UNIQUE INDEX IF NOT EXISTS uq_hre_historial_recibo
    ON historial_recibos_electronicos (historial_recibo_id)
    WHERE historial_recibo_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_hre_abono_cxc
    ON historial_recibos_electronicos (abono_cxc_id)
    WHERE abono_cxc_id IS NOT NULL;

ALTER TABLE historial_recibos_electronicos
    DROP CONSTRAINT IF EXISTS ck_hre_origen_xor;

ALTER TABLE historial_recibos_electronicos
    ADD CONSTRAINT ck_hre_origen_xor CHECK (
        (historial_recibo_id IS NOT NULL AND abono_cxc_id IS NULL)
        OR (historial_recibo_id IS NULL AND abono_cxc_id IS NOT NULL)
    );

COMMENT ON COLUMN historial_recibos_electronicos.abono_cxc_id IS
    'Origen abono CxC (QR/Bancolombia). Mutuamente excluyente con historial_recibo_id.';
