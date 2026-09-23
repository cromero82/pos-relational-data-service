-- Plan 2: catálogo persona + egreso.persona_id (pagos PERSONAL/DIVIDENDOS sin proveedor).

CREATE TABLE IF NOT EXISTS persona (
    id              BIGSERIAL PRIMARY KEY,
    documento       VARCHAR(50) NOT NULL,
    nombre          VARCHAR(150) NOT NULL,
    telefono        VARCHAR(30),
    correo          VARCHAR(100),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_persona_documento UNIQUE (documento)
);

COMMENT ON TABLE persona IS
    'Tercero persona (empleado/ocasional/dueño). No es liquidación de nómina.';

ALTER TABLE egreso
    ADD COLUMN IF NOT EXISTS persona_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_egreso_persona'
    ) THEN
        ALTER TABLE egreso
            ADD CONSTRAINT fk_egreso_persona
            FOREIGN KEY (persona_id) REFERENCES persona (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_egreso_persona_id ON egreso (persona_id);

-- proveedor puede ser null cuando el beneficiario es persona
ALTER TABLE egreso ALTER COLUMN proveedor_id DROP NOT NULL;

COMMENT ON COLUMN egreso.persona_id IS
    'Beneficiario persona (PERSONAL/DIVIDENDOS). Excluyente con proveedor_id.';
