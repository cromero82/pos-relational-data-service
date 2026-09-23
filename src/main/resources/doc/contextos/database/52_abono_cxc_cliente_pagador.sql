-- 52 — Quién entrega el abono (puede ser distinto del cliente de la CxC)

ALTER TABLE abono_cxc
    ADD COLUMN IF NOT EXISTS cliente_pagador_id BIGINT REFERENCES client (id);

ALTER TABLE abono_cxc
    ADD COLUMN IF NOT EXISTS cliente_pagador_nombre VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_abono_cxc_pagador
    ON abono_cxc (cliente_pagador_id);

COMMENT ON COLUMN abono_cxc.cliente_pagador_id IS
    'Cliente que entrega el dinero del abono (puede diferir del deudor de la CxC).';

COMMENT ON COLUMN abono_cxc.cliente_pagador_nombre IS
    'Snapshot del nombre al registrar el abono.';
