-- Sprint 4 — Origen de pago en egresos y flags en metodo_pago

ALTER TABLE egreso
    ADD COLUMN IF NOT EXISTS metodo_pago_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_egreso_metodo_pago'
    ) THEN
        ALTER TABLE egreso
            ADD CONSTRAINT fk_egreso_metodo_pago
            FOREIGN KEY (metodo_pago_id) REFERENCES metodo_pago (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_egreso_metodo_pago ON egreso (metodo_pago_id);
CREATE INDEX IF NOT EXISTS idx_egreso_fecha_creacion ON egreso (fecha_creacion);

ALTER TABLE metodo_pago
    ADD COLUMN IF NOT EXISTS visible_pagos_egresos BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE metodo_pago
    ADD COLUMN IF NOT EXISTS es_base_proveedores BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE metodo_pago SET visible_pagos_egresos = TRUE WHERE visible_pagos_egresos IS NULL;
UPDATE metodo_pago SET es_base_proveedores = FALSE WHERE es_base_proveedores IS NULL;
