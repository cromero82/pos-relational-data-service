-- Sprint B5 — Motivo de desfase por medio + seeds categoría DESFASE_CIERRE

ALTER TABLE ventas_tipo
    ADD COLUMN IF NOT EXISTS motivo_desfase_id INTEGER NULL
        REFERENCES motivo_movimiento (id);

CREATE INDEX IF NOT EXISTS idx_ventas_tipo_motivo_desfase
    ON ventas_tipo (motivo_desfase_id);

INSERT INTO motivo_movimiento (codigo, nombre, categoria, sistema, orden)
VALUES
    ('DESCONOCIDO', 'Desconocido', 'DESFASE_CIERRE', TRUE, 10),
    ('ERROR_MEDIO_PAGO', 'Error humano: medio de pago equivocado', 'DESFASE_CIERRE', TRUE, 20),
    ('ERROR_CONTEO', 'Error de conteo físico', 'DESFASE_CIERRE', TRUE, 30),
    ('FALTA_CAMBIO', 'Falta o sobra de cambio', 'DESFASE_CIERRE', TRUE, 40),
    ('AJUSTE_PERSONAL_CIERRE', 'Ajuste con dinero personal del administrador', 'DESFASE_CIERRE', TRUE, 50),
    ('OTRO_DESFASE', 'Otro', 'DESFASE_CIERRE', TRUE, 99)
ON CONFLICT (codigo) DO NOTHING;
