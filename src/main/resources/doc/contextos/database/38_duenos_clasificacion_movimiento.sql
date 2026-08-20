-- 38 — Raíz «Dueños» (no operativo) + clasificación operativa en movimientos
-- Idempotente. Reubica «Personal administrador» → «Cuenta del dueño» bajo Dueños.
-- DnD/reporte: mini-sprint posterior; este archivo deja contrato + árbol.

-- Tipo de OF para cuentas no operativas del día
INSERT INTO tipo_origen_fondos (codigo, nombre, descripcion)
VALUES (
    'DUENOS',
    'Dueños / no operativo',
    'Cuentas con el dueño o socios: retiros personales, due-to/due-from. No es caja del turno ni gasto P&L.'
)
ON CONFLICT (codigo) DO UPDATE
SET nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion;

-- Clasificación operativa en ledger (misma familia que legalizar notificación)
ALTER TABLE movimiento_origen_fondos
    ADD COLUMN IF NOT EXISTS clasificacion_operativa VARCHAR(40);

ALTER TABLE movimiento_origen_fondos
    ADD COLUMN IF NOT EXISTS periodo_cierre_id INTEGER;

COMMENT ON COLUMN movimiento_origen_fondos.clasificacion_operativa IS
    'CUENTA_PERSONAL | ANTICIPO_SALARIO | VALE_EMPLEADO | GASTO_NEGOCIO | OTRO_LEGALIZADO | NULL. '
    'Tag de negocio distinto del OF (dónde está la plata). Para reporte operativo / cierre de periodo.';

COMMENT ON COLUMN movimiento_origen_fondos.periodo_cierre_id IS
    'Reserva: id de cierre de periodo operativo (mensual). NULL = abierto. Tabla de periodos en sprint futuro.';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_movimiento_clasificacion_operativa'
    ) THEN
        ALTER TABLE movimiento_origen_fondos
            ADD CONSTRAINT ck_movimiento_clasificacion_operativa
            CHECK (
                clasificacion_operativa IS NULL
                OR clasificacion_operativa IN (
                    'CUENTA_PERSONAL',
                    'ANTICIPO_SALARIO',
                    'VALE_EMPLEADO',
                    'GASTO_NEGOCIO',
                    'OTRO_LEGALIZADO'
                )
            );
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_movimiento_clasificacion_operativa
    ON movimiento_origen_fondos (clasificacion_operativa)
    WHERE clasificacion_operativa IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_movimiento_periodo_cierre
    ON movimiento_origen_fondos (periodo_cierre_id)
    WHERE periodo_cierre_id IS NOT NULL;

-- Árbol: raíz Dueños + hijo Cuenta del dueño
DO $$
DECLARE
  tipo_duenos_id INTEGER;
  tipo_ahorro_id INTEGER;
  tipo_id INTEGER;
  root_id INTEGER;
  cuenta_id INTEGER;
  old_personal_id INTEGER;
BEGIN
  SELECT id INTO tipo_duenos_id FROM tipo_origen_fondos WHERE codigo = 'DUENOS' LIMIT 1;
  SELECT id INTO tipo_ahorro_id FROM tipo_origen_fondos WHERE codigo = 'AHORRO' LIMIT 1;
  tipo_id := COALESCE(tipo_duenos_id, tipo_ahorro_id);
  IF tipo_id IS NULL THEN
    RAISE NOTICE '38: no hay tipo DUENOS/AHORRO — skip árbol Dueños';
    RETURN;
  END IF;

  SELECT id INTO root_id FROM origen_fondos
  WHERE parent_origen_fondos_id IS NULL
    AND LOWER(btrim(nombre)) IN ('dueños', 'duenos', 'dueños / no operativos', 'no operativos')
  ORDER BY id LIMIT 1;

  IF root_id IS NULL THEN
    INSERT INTO origen_fondos (
      nombre, tipo_origen_fondos_id, parent_origen_fondos_id, estado,
      visible_en_egreso, requiere_conciliacion, activo, orden, naturaleza, metodo_pago_id, color, notas
    ) VALUES (
      'Dueños',
      tipo_id,
      NULL,
      'ACTIVO',
      FALSE,
      FALSE,
      TRUE,
      200,
      'ELECTRONICA',
      NULL,
      '#6D4C41',
      'Raíz no operativa: cuentas con el dueño/socios. No entra al arqueo del turno.'
    )
    RETURNING id INTO root_id;
  ELSE
    UPDATE origen_fondos
    SET nombre = 'Dueños',
        tipo_origen_fondos_id = tipo_id,
        visible_en_egreso = FALSE,
        orden = COALESCE(orden, 200),
        notas = COALESCE(
            notas,
            'Raíz no operativa: cuentas con el dueño/socios. No entra al arqueo del turno.'
        ),
        activo = TRUE,
        estado = 'ACTIVO'
    WHERE id = root_id;
  END IF;

  -- Preferir cuenta ya llamada Cuenta del dueño bajo Dueños
  SELECT id INTO cuenta_id FROM origen_fondos
  WHERE parent_origen_fondos_id = root_id
    AND LOWER(btrim(nombre)) IN ('cuenta del dueño', 'cuenta del dueno', 'personal administrador')
  ORDER BY id LIMIT 1;

  -- Reubicar Personal administrador (p.ej. bajo Caja: Efectivo) si existe fuera de Dueños
  SELECT id INTO old_personal_id FROM origen_fondos
  WHERE LOWER(btrim(nombre)) IN ('personal administrador', 'cuenta del dueño', 'cuenta del dueno')
    AND (parent_origen_fondos_id IS DISTINCT FROM root_id)
  ORDER BY id LIMIT 1;

  IF cuenta_id IS NULL AND old_personal_id IS NOT NULL THEN
    UPDATE origen_fondos
    SET parent_origen_fondos_id = root_id,
        nombre = 'Cuenta del dueño',
        tipo_origen_fondos_id = tipo_id,
        visible_en_egreso = FALSE,
        orden = 10,
        metodo_pago_id = NULL,
        notas = COALESCE(
            notas,
            'Due-to/due-from con el dueño. Retiros personales ≠ gasto del negocio (P&L).'
        ),
        activo = TRUE,
        estado = 'ACTIVO'
    WHERE id = old_personal_id;
    cuenta_id := old_personal_id;
  ELSIF cuenta_id IS NOT NULL THEN
    UPDATE origen_fondos
    SET nombre = 'Cuenta del dueño',
        tipo_origen_fondos_id = tipo_id,
        visible_en_egreso = FALSE,
        orden = 10,
        metodo_pago_id = NULL,
        notas = COALESCE(
            notas,
            'Due-to/due-from con el dueño. Retiros personales ≠ gasto del negocio (P&L).'
        ),
        activo = TRUE,
        estado = 'ACTIVO'
    WHERE id = cuenta_id;
  ELSE
    INSERT INTO origen_fondos (
      nombre, tipo_origen_fondos_id, parent_origen_fondos_id, estado,
      visible_en_egreso, requiere_conciliacion, activo, orden, naturaleza, metodo_pago_id, notas
    ) VALUES (
      'Cuenta del dueño',
      tipo_id,
      root_id,
      'ACTIVO',
      FALSE,
      FALSE,
      TRUE,
      10,
      'ELECTRONICA',
      NULL,
      'Due-to/due-from con el dueño. Retiros personales ≠ gasto del negocio (P&L).'
    );
  END IF;

  -- Backfill: legalizaciones ya hechas hacia esa cuenta
  IF cuenta_id IS NOT NULL THEN
    UPDATE movimiento_origen_fondos m
    SET clasificacion_operativa = 'CUENTA_PERSONAL'
    WHERE m.clasificacion_operativa IS NULL
      AND m.origen_tipo = 'LEGALIZACION_NOTIFICACION'
      AND m.origen_fondos_id = cuenta_id
      AND m.impacto > 0;
  END IF;

EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE '38: skip árbol Dueños: %', SQLERRM;
END $$;
