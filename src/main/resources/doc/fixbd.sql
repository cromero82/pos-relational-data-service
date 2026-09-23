-- ============================================================
-- FIX: Resincronización de sequences PostgreSQL
-- Causa: sequences desincronizados tras restore de backup
--        (datos insertados con IDs explícitos > last_value del sequence)
-- Ejecutar en: controlneg_rmx_db
-- ============================================================

SELECT setval('bitacora_usuario_id_seq',           COALESCE((SELECT MAX(id) FROM bitacora_usuario), 1));
SELECT setval('cargue_producto_conflictos_id_seq', COALESCE((SELECT MAX(id) FROM cargue_producto_conflictos), 1));
SELECT setval('cargue_productos_id_seq',           COALESCE((SELECT MAX(id) FROM cargue_productos), 1));
SELECT setval('client_id_seq',                     COALESCE((SELECT MAX(id) FROM client), 1));
SELECT setval('configuracion_app_id_seq',          COALESCE((SELECT MAX(id) FROM configuracion_app), 1));
SELECT setval('corte_venta_id_seq',                COALESCE((SELECT MAX(id) FROM corte_venta), 1));
SELECT setval('edicion_recibo_id_seq',             COALESCE((SELECT MAX(id) FROM edicion_recibo), 1));
SELECT setval('edicion_recibo_detalle_id_seq',     COALESCE((SELECT MAX(id) FROM edicion_recibo_detalle), 1));
SELECT setval('egreso_id_seq',                     COALESCE((SELECT MAX(id) FROM egreso), 1));
SELECT setval('estadistica_fin_id_seq',            COALESCE((SELECT MAX(id) FROM estadistica_fin), 1));
SELECT setval('estado_recibos_id_seq',             COALESCE((SELECT MAX(id) FROM estado_recibos), 1));
SELECT setval('evento_id_seq',                     COALESCE((SELECT MAX(id) FROM evento), 1));
SELECT setval('flujo_dinero_id_seq',               COALESCE((SELECT MAX(id) FROM flujo_dinero), 1));
SELECT setval('grupo_espejo_id_seq',               COALESCE((SELECT MAX(id) FROM grupo_espejo), 1));
SELECT setval('historial_producto_id_seq',         COALESCE((SELECT MAX(id) FROM historial_producto), 1));
SELECT setval('historial_recibo_id_seq',           COALESCE((SELECT MAX(id) FROM historial_recibo), 1));
SELECT setval('historial_recibo_detalle_id_seq',   COALESCE((SELECT MAX(id) FROM historial_recibo_detalle), 1));
SELECT setval('metodo_pago_id_seq',                COALESCE((SELECT MAX(id) FROM metodo_pago), 1));
SELECT setval('producto_id_seq',                   COALESCE((SELECT MAX(id) FROM producto), 1));
SELECT setval('proveedor_id_seq',                  COALESCE((SELECT MAX(id) FROM proveedor), 1));
SELECT setval('recibo_id_seq',                     COALESCE((SELECT MAX(id) FROM recibo), 1));
SELECT setval('recibo_detalle_id_seq',             COALESCE((SELECT MAX(id) FROM recibo_detalle), 1));
SELECT setval('sesion_id_seq',                     COALESCE((SELECT MAX(id) FROM sesion), 1));
SELECT setval('ticket_id_seq',                     COALESCE((SELECT MAX(id) FROM ticket), 1));
SELECT setval('ticket_recibo_id_seq',              COALESCE((SELECT MAX(id) FROM ticket_recibo), 1));
SELECT setval('tipo_egreso_id_seq',                COALESCE((SELECT MAX(id) FROM tipo_egreso), 1));
SELECT setval('tipo_resultado_fin_id_seq',         COALESCE((SELECT MAX(id) FROM tipo_resultado_fin), 1));
SELECT setval('usuario_perfil_id_seq',             COALESCE((SELECT MAX(id) FROM usuario_perfil), 1));
SELECT setval('ventas_tipo_id_seq',                COALESCE((SELECT MAX(id) FROM ventas_tipo), 1));

-- ============================================================
-- TABLA: app_log
-- Almacena logs de nivel WARN y ERROR generados por Log4j2
-- via JdbcAppender (sin código Java adicional en cada clase)
-- ============================================================

CREATE TABLE IF NOT EXISTS public.app_log (
    id        BIGSERIAL    PRIMARY KEY,
    fecha     TIMESTAMP    NOT NULL DEFAULT NOW(),
    nivel     VARCHAR(10)  NOT NULL,
    logger    VARCHAR(255) NOT NULL,
    mensaje   TEXT         NOT NULL,
    excepcion TEXT,
    thread    VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_app_log_fecha  ON public.app_log (fecha DESC);
CREATE INDEX IF NOT EXISTS idx_app_log_nivel  ON public.app_log (nivel);
