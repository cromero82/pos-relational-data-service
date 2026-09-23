-- 36 — Legalizar notificaciones email (reclasificación de retiros/ingresos)

ALTER TABLE notificacion_email_pago
    ADD COLUMN IF NOT EXISTS clasificacion VARCHAR(40);

ALTER TABLE notificacion_email_pago
    ADD COLUMN IF NOT EXISTS clasificacion_observacion TEXT;

ALTER TABLE notificacion_email_pago
    ADD COLUMN IF NOT EXISTS clasificado_en TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE notificacion_email_pago
    ADD COLUMN IF NOT EXISTS clasificado_por UUID;

COMMENT ON COLUMN notificacion_email_pago.clasificacion IS
    'PENDIENTE | VALE_EMPLEADO | ANTICIPO_SALARIO | CUENTA_PERSONAL | GASTO_NEGOCIO | OTRO_LEGALIZADO';

-- Motivos de legalización (AJUSTE) para UI / ledger
INSERT INTO motivo_movimiento (codigo, nombre, categoria, sistema, orden, accion_esperada)
VALUES
    ('LEGALIZAR_VALE_EMPLEADO', 'Legalizar: vale / préstamo empleado', 'AJUSTE', TRUE, 70, 'REGISTRAR_DOCUMENTO'),
    ('LEGALIZAR_ANTICIPO_SALARIO', 'Legalizar: anticipo de salario', 'AJUSTE', TRUE, 71, 'REGISTRAR_DOCUMENTO'),
    ('LEGALIZAR_CUENTA_PERSONAL', 'Legalizar: cuenta personal administrador', 'AJUSTE', TRUE, 72, 'REGISTRAR_DOCUMENTO'),
    ('LEGALIZAR_GASTO_NEGOCIO', 'Legalizar: gasto del negocio', 'AJUSTE', TRUE, 73, 'REGISTRAR_DOCUMENTO')
ON CONFLICT (codigo) DO NOTHING;

-- OF hijo plantilla opcional — deprecado: preferir 38_duenos (raíz Dueños + Cuenta del dueño).
-- Se mantiene por installs viejos; 38 reubica «Personal administrador» si quedó bajo Efectivo.
DO $$
DECLARE
  root_id INTEGER;
  tipo_id INTEGER;
BEGIN
  -- Si ya existe raíz Dueños, no sembramos bajo Efectivo
  IF EXISTS (
      SELECT 1 FROM origen_fondos
      WHERE parent_origen_fondos_id IS NULL AND LOWER(btrim(nombre)) IN ('dueños', 'duenos')
  ) THEN
    RETURN;
  END IF;
  SELECT id INTO root_id FROM origen_fondos
  WHERE nombre ILIKE 'Caja: Efectivo' OR nombre ILIKE 'Caja Efectivo'
  ORDER BY id LIMIT 1;
  IF root_id IS NULL THEN
    SELECT id INTO root_id FROM origen_fondos
    WHERE parent_origen_fondos_id IS NULL ORDER BY id LIMIT 1;
  END IF;
  SELECT id INTO tipo_id FROM tipo_origen_fondos
  WHERE codigo IN ('AHORRO', 'BOLSILLO', 'OPERATIVO', 'CAJA')
  ORDER BY CASE codigo WHEN 'AHORRO' THEN 1 WHEN 'BOLSILLO' THEN 2 ELSE 3 END
  LIMIT 1;
  IF root_id IS NOT NULL AND tipo_id IS NOT NULL AND NOT EXISTS (
      SELECT 1 FROM origen_fondos
      WHERE nombre = 'Personal administrador' AND parent_origen_fondos_id = root_id
  ) THEN
    INSERT INTO origen_fondos (
      nombre, tipo_origen_fondos_id, parent_origen_fondos_id, estado,
      visible_en_egreso, requiere_conciliacion, activo, orden, naturaleza
    ) VALUES (
      'Personal administrador', tipo_id, root_id, 'ACTIVO',
      TRUE, FALSE, TRUE, 90, 'ELECTRONICA'
    );
  END IF;
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE '36: skip seed OF Personal administrador: %', SQLERRM;
END $$;
