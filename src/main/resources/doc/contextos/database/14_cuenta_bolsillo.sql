-- Sprint B1 — Cuentas bolsillo (cajitas / cuentas internas)

CREATE TABLE IF NOT EXISTS cuenta_bolsillo (
    id                      SERIAL PRIMARY KEY,
    nombre                  VARCHAR(120) NOT NULL,
    tipo_bolsillo_id        INTEGER NOT NULL REFERENCES tipo_bolsillo (id),
    proveedor_id            BIGINT NULL REFERENCES proveedor (id),
    metodo_pago_id          BIGINT NULL REFERENCES metodo_pago (id),
    naturaleza              VARCHAR(20) NOT NULL DEFAULT 'ELECTRONICA',
    visible_en_egreso       BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_conciliacion   BOOLEAN NOT NULL DEFAULT FALSE,
    activo                  BOOLEAN NOT NULL DEFAULT TRUE,
    orden                   INTEGER NOT NULL DEFAULT 0,
    color                   VARCHAR(20),
    notas                   TEXT,
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cuenta_bolsillo_metodo_pago ON cuenta_bolsillo (metodo_pago_id);
CREATE INDEX IF NOT EXISTS idx_cuenta_bolsillo_tipo ON cuenta_bolsillo (tipo_bolsillo_id);

-- Backfill: una cuenta por metodo_pago activo
INSERT INTO cuenta_bolsillo (
    nombre, tipo_bolsillo_id, metodo_pago_id, naturaleza, visible_en_egreso, orden, color
)
SELECT
    COALESCE(mp.descripcion_egreso, mp.descripcion),
    (SELECT id FROM tipo_bolsillo WHERE codigo = 'OPERATIVO' LIMIT 1),
    mp.id,
    CASE WHEN mp.id = 1 THEN 'FISICA' ELSE 'ELECTRONICA' END,
    mp.visible_pagos_egresos,
    mp.id::INTEGER,
    mp.color
FROM metodo_pago mp
WHERE NOT EXISTS (
      SELECT 1 FROM cuenta_bolsillo cb WHERE cb.metodo_pago_id = mp.id
  );

-- Cuenta reserva proveedores (sin metodo_pago obligatorio) si no existe
INSERT INTO cuenta_bolsillo (nombre, tipo_bolsillo_id, naturaleza, visible_en_egreso, orden)
SELECT
    'Reserva pago proveedores',
    (SELECT id FROM tipo_bolsillo WHERE codigo = 'RESERVA_PROVEEDORES' LIMIT 1),
    'MIXTA',
    FALSE,
    100
WHERE NOT EXISTS (
    SELECT 1 FROM cuenta_bolsillo WHERE nombre = 'Reserva pago proveedores'
);
