-- Datos de ejemplo: CHOCOCONO (producto_id 656) — 6 cambios de precio en ~3 años
-- Requiere egresos existentes; crea entradas confirmadas mínimas si hace falta.

DO $$
DECLARE
    v_producto_id BIGINT := 656;
    v_egreso_id INTEGER;
    v_entrada_id BIGINT;
    v_detalle_id BIGINT;
    v_fecha TIMESTAMP;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM producto WHERE id = v_producto_id) THEN
        RAISE NOTICE 'Producto 656 no encontrado; omitiendo seed.';
        RETURN;
    END IF;

    -- Limpiar seed previo de demostración (detalles marcados en observaciones de egreso)
    DELETE FROM historial_precio_producto h
    USING entrada_inventario_detalle d, entrada_inventario e, egreso eg
    WHERE h.entrada_inventario_detalle_id = d.id
      AND d.entrada_id = e.id
      AND e.egreso_id = eg.id
      AND eg.descripcion LIKE 'SEED-HIST-PRECIO-CHOCOCONO%';

    DELETE FROM entrada_inventario_detalle d
    USING entrada_inventario e, egreso eg
    WHERE d.entrada_id = e.id
      AND e.egreso_id = eg.id
      AND eg.descripcion LIKE 'SEED-HIST-PRECIO-CHOCOCONO%';

    DELETE FROM entrada_inventario e
    USING egreso eg
    WHERE e.egreso_id = eg.id
      AND eg.descripcion LIKE 'SEED-HIST-PRECIO-CHOCOCONO%';

    DELETE FROM egreso WHERE descripcion LIKE 'SEED-HIST-PRECIO-CHOCOCONO%';

    -- 1) Mar 2023
    INSERT INTO egreso (fecha, valor, descripcion, proveedor_id)
    VALUES ('2023-03-15', 180000, 'SEED-HIST-PRECIO-CHOCOCONO-1', NULL)
    RETURNING id INTO v_egreso_id;

    INSERT INTO entrada_inventario (egreso_id, estado, fecha_creacion, fecha_confirmacion, total_items)
    VALUES (v_egreso_id, 'CONFIRMADA', '2023-03-15 10:00:00', '2023-03-15 10:05:00', 1)
    RETURNING id INTO v_entrada_id;

    INSERT INTO entrada_inventario_detalle (
        entrada_id, producto_id, cantidad, precio_compra_registrado,
        precio_compra_anterior, precio_venta_actual, porcentaje_ganancia_calc
    ) VALUES (v_entrada_id, v_producto_id, 100, 1800, NULL, 2800, 56)
    RETURNING id INTO v_detalle_id;

    INSERT INTO historial_precio_producto (
        entrada_inventario_detalle_id, producto_id, fecha_creacion,
        precio_compra, precio_compra_antes, precio_venta, precio_venta_antes,
        porcentaje_ganancia, porcentaje_ganancia_antes
    ) VALUES (
        v_detalle_id, v_producto_id, '2023-03-15 10:05:00',
        1800, NULL, 3000, NULL, 67, NULL
    );

    -- 2) Nov 2023
    INSERT INTO egreso (fecha, valor, descripcion) VALUES ('2023-11-20', 195000, 'SEED-HIST-PRECIO-CHOCOCONO-2') RETURNING id INTO v_egreso_id;
    INSERT INTO entrada_inventario (egreso_id, estado, fecha_creacion, fecha_confirmacion, total_items)
    VALUES (v_egreso_id, 'CONFIRMADA', '2023-11-20 09:00:00', '2023-11-20 09:10:00', 1) RETURNING id INTO v_entrada_id;
    INSERT INTO entrada_inventario_detalle (entrada_id, producto_id, cantidad, precio_compra_registrado, precio_compra_anterior, precio_venta_actual, porcentaje_ganancia_calc)
    VALUES (v_entrada_id, v_producto_id, 100, 1950, 1800, 3000, 54) RETURNING id INTO v_detalle_id;
    INSERT INTO historial_precio_producto (entrada_inventario_detalle_id, producto_id, fecha_creacion, precio_compra, precio_compra_antes, precio_venta, precio_venta_antes, porcentaje_ganancia, porcentaje_ganancia_antes)
    VALUES (v_detalle_id, v_producto_id, '2023-11-20 09:10:00', 1950, 1800, 3200, 3000, 64, 67);

    -- 3) Jun 2024 — solo sube compra
    INSERT INTO egreso (fecha, valor, descripcion) VALUES ('2024-06-08', 210000, 'SEED-HIST-PRECIO-CHOCOCONO-3') RETURNING id INTO v_egreso_id;
    INSERT INTO entrada_inventario (egreso_id, estado, fecha_creacion, fecha_confirmacion, total_items)
    VALUES (v_egreso_id, 'CONFIRMADA', '2024-06-08 14:00:00', '2024-06-08 14:08:00', 1) RETURNING id INTO v_entrada_id;
    INSERT INTO entrada_inventario_detalle (entrada_id, producto_id, cantidad, precio_compra_registrado, precio_compra_anterior, precio_venta_actual, porcentaje_ganancia_calc)
    VALUES (v_entrada_id, v_producto_id, 100, 2100, 1950, 3200, 52) RETURNING id INTO v_detalle_id;
    INSERT INTO historial_precio_producto (entrada_inventario_detalle_id, producto_id, fecha_creacion, precio_compra, precio_compra_antes, precio_venta, precio_venta_antes, porcentaje_ganancia, porcentaje_ganancia_antes)
    VALUES (v_detalle_id, v_producto_id, '2024-06-08 14:08:00', 2100, 1950, 3500, 3200, 67, 64);

    -- 4) Ene 2025
    INSERT INTO egreso (fecha, valor, descripcion) VALUES ('2025-01-22', 225000, 'SEED-HIST-PRECIO-CHOCOCONO-4') RETURNING id INTO v_egreso_id;
    INSERT INTO entrada_inventario (egreso_id, estado, fecha_creacion, fecha_confirmacion, total_items)
    VALUES (v_egreso_id, 'CONFIRMADA', '2025-01-22 11:00:00', '2025-01-22 11:12:00', 1) RETURNING id INTO v_entrada_id;
    INSERT INTO entrada_inventario_detalle (entrada_id, producto_id, cantidad, precio_compra_registrado, precio_compra_anterior, precio_venta_actual, porcentaje_ganancia_calc)
    VALUES (v_entrada_id, v_producto_id, 100, 2250, 2100, 3500, 56) RETURNING id INTO v_detalle_id;
    INSERT INTO historial_precio_producto (entrada_inventario_detalle_id, producto_id, fecha_creacion, precio_compra, precio_compra_antes, precio_venta, precio_venta_antes, porcentaje_ganancia, porcentaje_ganancia_antes)
    VALUES (v_detalle_id, v_producto_id, '2025-01-22 11:12:00', 2250, 2100, 3800, 3500, 69, 67);

    -- 5) Sep 2025 — baja venta por promoción temporal (simulado)
    INSERT INTO egreso (fecha, valor, descripcion) VALUES ('2025-09-10', 230000, 'SEED-HIST-PRECIO-CHOCOCONO-5') RETURNING id INTO v_egreso_id;
    INSERT INTO entrada_inventario (egreso_id, estado, fecha_creacion, fecha_confirmacion, total_items)
    VALUES (v_egreso_id, 'CONFIRMADA', '2025-09-10 16:00:00', '2025-09-10 16:05:00', 1) RETURNING id INTO v_entrada_id;
    INSERT INTO entrada_inventario_detalle (entrada_id, producto_id, cantidad, precio_compra_registrado, precio_compra_anterior, precio_venta_actual, precio_venta_nuevo, porcentaje_ganancia_calc)
    VALUES (v_entrada_id, v_producto_id, 100, 2300, 2250, 3800, 3700, 61) RETURNING id INTO v_detalle_id;
    INSERT INTO historial_precio_producto (entrada_inventario_detalle_id, producto_id, fecha_creacion, precio_compra, precio_compra_antes, precio_venta, precio_venta_antes, porcentaje_ganancia, porcentaje_ganancia_antes)
    VALUES (v_detalle_id, v_producto_id, '2025-09-10 16:05:00', 2300, 2250, 3700, 3800, 61, 69);

    -- 6) Abr 2026 — estado actual catálogo
    INSERT INTO egreso (fecha, valor, descripcion) VALUES ('2026-04-19', 240000, 'SEED-HIST-PRECIO-CHOCOCONO-6') RETURNING id INTO v_egreso_id;
    INSERT INTO entrada_inventario (egreso_id, estado, fecha_creacion, fecha_confirmacion, total_items)
    VALUES (v_egreso_id, 'CONFIRMADA', '2026-04-19 18:38:00', '2026-04-19 18:38:15', 1) RETURNING id INTO v_entrada_id;
    INSERT INTO entrada_inventario_detalle (entrada_id, producto_id, cantidad, precio_compra_registrado, precio_compra_anterior, precio_venta_actual, porcentaje_ganancia_calc)
    VALUES (v_entrada_id, v_producto_id, 100, 2400, 2300, 3700, 54) RETURNING id INTO v_detalle_id;
    INSERT INTO historial_precio_producto (entrada_inventario_detalle_id, producto_id, fecha_creacion, precio_compra, precio_compra_antes, precio_venta, precio_venta_antes, porcentaje_ganancia, porcentaje_ganancia_antes)
    VALUES (v_detalle_id, v_producto_id, '2026-04-19 18:38:15', 2400, 2300, 4000, 3700, 67, 61);

    RAISE NOTICE 'Seed historial CHOCOCONO completado.';
END $$;
