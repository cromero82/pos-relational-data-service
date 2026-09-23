-- =============================================================================
-- 51_producto_presentacion_migracion_final.sql
-- Sprint final: asegurar presentaciones para todos, backfill líneas históricas
-- por inferencia de precio, sync columnas duales de producto (lectura).
-- Ajustar factor_a_base real de PAQUETE queda como captura operativa (UPDATE manual
-- o planilla); este script NO inventa factores > 1.
-- =============================================================================

-- 1) Todo producto activo/inactivo debe tener PAQUETE
INSERT INTO producto_presentacion (
    producto_id, codigo, nombre_mostrar, factor_a_base, precio_venta,
    es_default_venta, activo
)
SELECT
    p.id,
    'PAQUETE',
    'Paquete / presentación',
    1,
    COALESCE(p.precio, 0),
    TRUE,
    TRUE
FROM producto p
WHERE NOT EXISTS (
    SELECT 1 FROM producto_presentacion pp
    WHERE pp.producto_id = p.id AND pp.codigo = 'PAQUETE'
);

-- 2) Menudeo → UNIDAD si falta
INSERT INTO producto_presentacion (
    producto_id, codigo, nombre_mostrar, factor_a_base, precio_venta,
    es_default_venta, activo
)
SELECT
    p.id,
    'UNIDAD',
    'Unidad (menudeo)',
    1,
    COALESCE(p.precio_unidad, 0),
    FALSE,
    TRUE
FROM producto p
WHERE p.precio_unidad IS NOT NULL
  AND p.precio_unidad > 0
  AND NOT EXISTS (
      SELECT 1 FROM producto_presentacion pp
      WHERE pp.producto_id = p.id AND pp.codigo = 'UNIDAD'
  );

-- 3) Sync precios de presentación desde producto (fuente de verdad actual)
UPDATE producto_presentacion pp
SET precio_venta = COALESCE(p.precio, 0),
    fecha_actualizacion = NOW()
FROM producto p
WHERE pp.producto_id = p.id
  AND pp.codigo = 'PAQUETE';

UPDATE producto_presentacion pp
SET precio_venta = COALESCE(p.precio_unidad, 0),
    fecha_actualizacion = NOW()
FROM producto p
WHERE pp.producto_id = p.id
  AND pp.codigo = 'UNIDAD'
  AND p.precio_unidad IS NOT NULL
  AND p.precio_unidad > 0;

-- 4) Backfill líneas recibo_detalle sin presentación → PAQUETE default
UPDATE recibo_detalle rd
SET presentacion_id = pp.id,
    precio_unitario_snapshot = COALESCE(rd.precio_unitario_snapshot, pp.precio_venta),
    factor_snapshot = COALESCE(rd.factor_snapshot, pp.factor_a_base),
    cantidad_base = COALESCE(
        rd.cantidad_base,
        rd.cantidad * COALESCE(rd.factor_snapshot, pp.factor_a_base)
    )
FROM producto_presentacion pp
WHERE rd.presentacion_id IS NULL
  AND pp.producto_id = rd.producto_id
  AND pp.codigo = 'PAQUETE'
  AND pp.es_default_venta = TRUE;

-- Inferir UNIDAD en recibo_detalle si precio unitario ≈ precio_unidad
UPDATE recibo_detalle rd
SET presentacion_id = pp_u.id,
    precio_unitario_snapshot = pp_u.precio_venta,
    factor_snapshot = pp_u.factor_a_base,
    cantidad_base = rd.cantidad * pp_u.factor_a_base
FROM producto p
JOIN producto_presentacion pp_u
  ON pp_u.producto_id = p.id AND pp_u.codigo = 'UNIDAD' AND pp_u.activo = TRUE
WHERE rd.producto_id = p.id
  AND p.precio_unidad IS NOT NULL
  AND p.precio_unidad > 0
  AND rd.cantidad > 0
  AND ABS((rd.subtotal / rd.cantidad) - p.precio_unidad) < 0.02;

-- 5) Historial líneas
UPDATE historial_recibo_detalle hrd
SET presentacion_id = pp.id,
    precio_unitario_snapshot = COALESCE(hrd.precio_unitario_snapshot, pp.precio_venta),
    factor_snapshot = COALESCE(hrd.factor_snapshot, pp.factor_a_base),
    cantidad_base = COALESCE(
        hrd.cantidad_base,
        hrd.cantidad * COALESCE(hrd.factor_snapshot, pp.factor_a_base)
    )
FROM producto_presentacion pp
WHERE hrd.presentacion_id IS NULL
  AND pp.producto_id = hrd.producto_id
  AND pp.codigo = 'PAQUETE'
  AND pp.es_default_venta = TRUE;

UPDATE historial_recibo_detalle hrd
SET presentacion_id = pp_u.id,
    precio_unitario_snapshot = pp_u.precio_venta,
    factor_snapshot = pp_u.factor_a_base,
    cantidad_base = hrd.cantidad * pp_u.factor_a_base
FROM producto p
JOIN producto_presentacion pp_u
  ON pp_u.producto_id = p.id AND pp_u.codigo = 'UNIDAD' AND pp_u.activo = TRUE
WHERE hrd.producto_id = p.id
  AND p.precio_unidad IS NOT NULL
  AND p.precio_unidad > 0
  AND hrd.cantidad > 0
  AND ABS((hrd.subtotal / hrd.cantidad) - p.precio_unidad) < 0.02;

-- 6) edicion_recibo_detalle
UPDATE edicion_recibo_detalle erd
SET presentacion_id = pp.id,
    precio_unitario_snapshot = COALESCE(erd.precio_unitario_snapshot, pp.precio_venta),
    factor_snapshot = COALESCE(erd.factor_snapshot, pp.factor_a_base),
    cantidad_base = COALESCE(
        erd.cantidad_base,
        erd.cantidad * COALESCE(erd.factor_snapshot, pp.factor_a_base)
    )
FROM producto_presentacion pp
WHERE erd.presentacion_id IS NULL
  AND pp.producto_id = erd.producto_id
  AND pp.codigo = 'PAQUETE';

COMMENT ON COLUMN producto.precio IS
    'DEPRECATED como fuente de venta: usar producto_presentacion(PAQUETE).precio_venta. Se mantiene sync para compat.';
COMMENT ON COLUMN producto.precio_unidad IS
    'DEPRECATED como fuente de venta: usar producto_presentacion(UNIDAD).precio_venta. Se mantiene sync para compat.';
