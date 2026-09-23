-- Jerarquía: bolsillos hijos bajo un origen (medio de pago / cuenta raíz)

ALTER TABLE cuenta_bolsillo
    ADD COLUMN IF NOT EXISTS parent_cuenta_bolsillo_id INTEGER NULL
        REFERENCES cuenta_bolsillo (id);

CREATE INDEX IF NOT EXISTS idx_cuenta_bolsillo_parent
    ON cuenta_bolsillo (parent_cuenta_bolsillo_id);

-- Ejemplos de bolsillos hijos bajo cuenta operativa Bancolombia/QR (metodo_pago id=2)
INSERT INTO cuenta_bolsillo (
    nombre, tipo_bolsillo_id, parent_cuenta_bolsillo_id, naturaleza, visible_en_egreso, orden
)
SELECT
    'Bolsillo Nómina',
    (SELECT id FROM tipo_bolsillo WHERE codigo = 'AHORRO' LIMIT 1),
    cb.id,
    'ELECTRONICA',
    TRUE,
    201
FROM cuenta_bolsillo cb
WHERE cb.metodo_pago_id = 2
  AND cb.parent_cuenta_bolsillo_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM cuenta_bolsillo x WHERE x.nombre = 'Bolsillo Nómina'
  );

INSERT INTO cuenta_bolsillo (
    nombre, tipo_bolsillo_id, parent_cuenta_bolsillo_id, naturaleza, visible_en_egreso, orden
)
SELECT
    'Arriendo local',
    (SELECT id FROM tipo_bolsillo WHERE codigo = 'ARRIENDO' LIMIT 1),
    cb.id,
    'ELECTRONICA',
    TRUE,
    202
FROM cuenta_bolsillo cb
WHERE cb.metodo_pago_id = 2
  AND cb.parent_cuenta_bolsillo_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM cuenta_bolsillo x WHERE x.nombre = 'Arriendo local'
  );
