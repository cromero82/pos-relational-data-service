-- Sincroniza catálogos paramétricos desde la laptop (controlneg_rmx_db)
-- hacia controlneg_rmx_db_v02. No toca tickets/historial ni el ledger OF.
-- Ejecutar SOLO sobre controlneg_rmx_db_v02.

ALTER TABLE configuracion_app
    ADD COLUMN IF NOT EXISTS leyenda VARCHAR(255);

-- Contadores de catálogo de v02 se conservan (3417 productos).
-- No se copia `sistema.sandbox` (reset transaccional sobre datos de prod).

CREATE TEMP TABLE cfg_src (
    k VARCHAR(50) PRIMARY KEY,
    v VARCHAR(1000) NOT NULL,
    ley VARCHAR(255)
);
INSERT INTO cfg_src (k, v, ley) VALUES
    ('longitud-vertical-panel-productos', '650', NULL),
    ('alerta-precios', '{"porcentaje_minimo":10,"porc_maximo":80}', NULL),
    ('ultimo-corte-egresos', '2026-03-28', NULL),
    ('notificaciones.qr.asuntos-permitidos', '{"permitidas":["Alertas y Notificaciones"]}', NULL),
    ('monitor-bug', '{"mostrar":true}', NULL),
    ('corte-venta.limite-permitido-revisada', '20000', NULL),
    ('alertas.creditos', '{"tiempoRiesgos":{"normal":5,"medio":15,"alto":35}}', NULL),
    ('notificaciones.qr.tiempo-luego-ya-no-esperar', '30',
     'Tiempo de espera a que se confirme transaccion (correo) luego de cajero hizo click "ya no esperar"'),
    ('tiempo.consulta-notificaciones', '10',
     'Tiempo en minutos en el cual se ejecutara endpoint de consulta del componente de notificaciones'),
    ('notificaciones.activa', 'true', NULL),
    ('corte-venta.base-efectivo', '150000', 'Base dinero en efectivo sugerida para las cajas'),
    ('notificaciones.asociaciones-egresos.obligatorio', 'false', NULL);

UPDATE configuracion_app c
SET value = s.v,
    leyenda = COALESCE(s.ley, c.leyenda)
FROM cfg_src s
WHERE c.key = s.k;

INSERT INTO configuracion_app (key, value, leyenda)
SELECT s.k, s.v, s.ley
FROM cfg_src s
WHERE NOT EXISTS (SELECT 1 FROM configuracion_app c WHERE c.key = s.k);

-- Nequi debe extraer correos igual que QR.
UPDATE metodo_pago
SET permite_notificacion = TRUE
WHERE id = 3
   OR UPPER(TRIM(COALESCE(sigla, ''))) = 'NEQUI';

UPDATE tipo_egreso
SET nombre = 'Otro tipo de egreso     Otro tipo de egreso',
    descripcion = 'Otro tipo de egreso     Otro tipo de egreso',
    naturaleza_tipo_egreso_id = 6
WHERE id = 12;

INSERT INTO tipo_egreso (id, nombre, descripcion, naturaleza_tipo_egreso_id)
SELECT 13,
       'Adecuaciones, eventos o fechas especiales       Gasto del establecimiento asociados a eventos, fiestas patrias o decoraciones del establecimiento',
       'Adecuaciones, eventos o fechas especiales       Gasto del establecimiento asociados a eventos, fiestas patrias o decoraciones del establecimiento',
       2
WHERE NOT EXISTS (SELECT 1 FROM tipo_egreso WHERE id = 13);

SELECT setval('tipo_egreso_id_seq', GREATEST((SELECT MAX(id) FROM tipo_egreso), 1));

-- Plantillas EGRESO apuntan a Bancolombia QR → Sin Clasificar (hijo).
INSERT INTO origen_fondos (
    nombre, tipo_origen_fondos_id, naturaleza, visible_en_egreso,
    requiere_conciliacion, activo, orden, color, parent_origen_fondos_id, estado
)
SELECT 'Sin Clasificar', 1, 'ELECTRONICA', TRUE, FALSE, TRUE, 203, '#0033A0',
       (SELECT id FROM origen_fondos WHERE nombre = 'Bancolombia - QR' AND parent_origen_fondos_id IS NULL LIMIT 1),
       'ACTIVO'
WHERE NOT EXISTS (
    SELECT 1 FROM origen_fondos WHERE lower(btrim(nombre)) = 'sin clasificar'
);

CREATE TEMP TABLE pnp_src (
    nombre VARCHAR(40) PRIMARY KEY,
    cuerpo TEXT NOT NULL,
    icono VARCHAR(120),
    activo BOOLEAN NOT NULL,
    orden INTEGER NOT NULL,
    naturaleza VARCHAR(20),
    origen_tipo VARCHAR(80) NOT NULL,
    metodo_pago_id BIGINT,
    of_origen_nombre VARCHAR(120),
    of_dest_nombre VARCHAR(120)
);
INSERT INTO pnp_src VALUES
    ('QR', 'Bancolombia: AUTOSERVICIO INFINITO, recibiste un pago de {{nombrePagador}} por {{monto}} en tu cuenta *{{referenciaCuenta}}',
     'qr-bancolombia.png', TRUE, 1, 'INGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, NULL, NULL),
    ('BREVE', 'Bancolombia: AUTOSERVICIO INFINITO, recibiste una transferencia de {{nombrePagador}} por {{monto}} en tu cuenta *{{referenciaCuenta}} conectada a la llave 86070384 el 31/07/26 a las 20:34. Con llaves es de una y gratis. Dudas al 018000912345',
     'breve-logo.png', TRUE, 2, 'INGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, NULL, NULL),
    ('EGRESO LULO', 'Realizaste una compra en {{nombrePagador}} por {{monto}}',
     'otro-metodo.png', TRUE, 4, 'EGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, 'Bancolombia - QR', 'Sin Clasificar'),
    ('PAGOS QR', 'Hiciste un pago a {{nombrePagador}} por {{monto}}',
     'qr-bancolombia.png', TRUE, 5, 'EGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, 'Bancolombia - QR', 'Sin Clasificar'),
    ('BANCOLOMBA CARLOS INFINITO', 'Bancolombia: CARLOS, recibiste una transferencia de {{nombrePagador}} por {{monto}} en tu cuenta *{{referenciaCuenta}}',
     'qr-bancolombia.png', TRUE, 6, 'INGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, 'Bancolombia - QR', 'Sin Clasificar'),
    ('TRANSFERENCIA EGRESO', 'Bancolombia: Transferiste {{monto}} desde tu cuenta *{{referenciaCuenta}} a la cuenta',
     'qr-bancolombia.png', TRUE, 7, 'EGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, 'Bancolombia - QR', 'Sin Clasificar'),
    ('COMPRASTE', 'Compraste {{monto}} en {{nombrePagador}} con',
     'qr-bancolombia.png', TRUE, 8, 'EGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, 'Bancolombia - QR', 'Sin Clasificar'),
    ('RETIRO BANCOLOMBIA', 'Retiraste {{monto}} en {{lugarRetiro}} de tu',
     'qr-bancolombia.png', TRUE, 9, 'EGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', NULL, 'Bancolombia - QR', 'Sin Clasificar'),
    ('INGRESO NOTIF NEQUI', 'Venta exitosa por {{monto}}',
     NULL, TRUE, 9, 'INGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 3, NULL, NULL),
    ('PAGASTE', 'pagaste {{monto}} por codigo QR desde tu cuenta',
     'qr-bancolombia.png', TRUE, 1, 'EGRESO', 'MOVIMIENTO BANCO POR IDENTIFICAR', 2, 'Bancolombia - QR', 'Sin Clasificar');

UPDATE plantilla_notificacion_pago p
SET cuerpo = s.cuerpo,
    icono = s.icono,
    activo = s.activo,
    orden = s.orden,
    naturaleza = s.naturaleza,
    origen_tipo = s.origen_tipo,
    metodo_pago_id = s.metodo_pago_id,
    origen_fondos_origen_id = (
        SELECT o.id FROM origen_fondos o
        WHERE s.of_origen_nombre IS NOT NULL AND o.nombre = s.of_origen_nombre
          AND o.parent_origen_fondos_id IS NULL
        LIMIT 1
    ),
    origen_fondos_destino_id = (
        SELECT o.id FROM origen_fondos o
        WHERE s.of_dest_nombre IS NOT NULL AND lower(btrim(o.nombre)) = lower(s.of_dest_nombre)
        LIMIT 1
    ),
    actualizado_en = CURRENT_TIMESTAMP
FROM pnp_src s
WHERE p.nombre = s.nombre;

INSERT INTO plantilla_notificacion_pago (
    nombre, cuerpo, icono, activo, orden, naturaleza, origen_tipo, metodo_pago_id,
    origen_fondos_origen_id, origen_fondos_destino_id
)
SELECT
    s.nombre, s.cuerpo, s.icono, s.activo, s.orden, s.naturaleza, s.origen_tipo, s.metodo_pago_id,
    (SELECT o.id FROM origen_fondos o
     WHERE s.of_origen_nombre IS NOT NULL AND o.nombre = s.of_origen_nombre
       AND o.parent_origen_fondos_id IS NULL LIMIT 1),
    (SELECT o.id FROM origen_fondos o
     WHERE s.of_dest_nombre IS NOT NULL AND lower(btrim(o.nombre)) = lower(s.of_dest_nombre) LIMIT 1)
FROM pnp_src s
WHERE NOT EXISTS (
    SELECT 1 FROM plantilla_notificacion_pago p WHERE p.nombre = s.nombre
);

SELECT setval(
    'plantilla_notificacion_pago_id_seq',
    GREATEST((SELECT MAX(id) FROM plantilla_notificacion_pago), 1)
);
SELECT setval(
    'configuracion_app_id_seq',
    GREATEST((SELECT MAX(id) FROM configuracion_app), 1)
);
