-- Persona dueño/propietario: habilita egreso desde Cuenta del dueño (visible_en_egreso sigue FALSE).

ALTER TABLE persona
    ADD COLUMN IF NOT EXISTS es_dueno_propietario BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN persona.es_dueno_propietario IS
    'Beneficiario dueño/propietario. Permite egreso PERSONAL/DIVIDENDOS con origen Cuenta del dueño.';
