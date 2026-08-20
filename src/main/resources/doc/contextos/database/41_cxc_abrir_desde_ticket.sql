-- 41 — CxC apertura desde ticket: correo cliente + vínculo recibo/ticket

ALTER TABLE client
    ADD COLUMN IF NOT EXISTS correo VARCHAR(255);

COMMENT ON COLUMN client.correo IS
    'Correo de contacto (opcional). No se fuerza a mayúsculas.';

ALTER TABLE cuenta_por_cobrar
    ADD COLUMN IF NOT EXISTS recibo_id BIGINT,
    ADD COLUMN IF NOT EXISTS ticket_id BIGINT;

COMMENT ON COLUMN cuenta_por_cobrar.recibo_id IS
    'Recibo vivo del ticket al abrir el crédito (antes de historial_recibo).';
COMMENT ON COLUMN cuenta_por_cobrar.ticket_id IS
    'Ticket POS asociado a la cuenta por cobrar.';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cxc_recibo'
    ) THEN
        ALTER TABLE cuenta_por_cobrar
            ADD CONSTRAINT fk_cxc_recibo
            FOREIGN KEY (recibo_id) REFERENCES recibo (id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cxc_ticket'
    ) THEN
        ALTER TABLE cuenta_por_cobrar
            ADD CONSTRAINT fk_cxc_ticket
            FOREIGN KEY (ticket_id) REFERENCES ticket (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_cxc_recibo ON cuenta_por_cobrar (recibo_id);
CREATE INDEX IF NOT EXISTS idx_cxc_ticket ON cuenta_por_cobrar (ticket_id);

-- Una sola CxC vigente por recibo
CREATE UNIQUE INDEX IF NOT EXISTS uq_cxc_recibo_vigente
    ON cuenta_por_cobrar (recibo_id)
    WHERE recibo_id IS NOT NULL
      AND estado IN ('ABIERTA', 'PARCIAL');
