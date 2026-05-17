-- ============================================================
-- MIGRACIÓN: Poblar tablas 1-N de métodos de pago
-- Archivo  : migrar-datos-metodo-pago.sql
-- Descripción: Copia los metodo_pago_id existentes en las tablas
--              recibo, historial_recibo y edicion_recibo hacia
--              las nuevas tablas de relación 1-N.
--              EJECUTAR DESPUÉS de nuevastablas-metodo-pago.sql
-- ============================================================

-- Verificación previa: mostrar cuántos registros existen
SELECT 'recibo con metodo_pago_id'             AS fuente, COUNT(*) AS total FROM public.recibo         WHERE metodo_pago_id IS NOT NULL
UNION ALL
SELECT 'historial_recibo con metodo_pago_id',            COUNT(*)          FROM public.historial_recibo WHERE metodo_pago_id IS NOT NULL
UNION ALL
SELECT 'edicion_recibo con metodo_pago_id',              COUNT(*)          FROM public.edicion_recibo   WHERE metodo_pago_id IS NOT NULL;

-- ------------------------------------------------------------
-- 1. Migrar desde recibo → recibo_metodo_pago
-- ------------------------------------------------------------
INSERT INTO public.recibo_metodo_pago (recibo_id, metodo_pago_id)
SELECT id, metodo_pago_id
FROM   public.recibo
WHERE  metodo_pago_id IS NOT NULL;

-- ------------------------------------------------------------
-- 2. Migrar desde historial_recibo → historial_recibo_metodo_pago
-- ------------------------------------------------------------
INSERT INTO public.historial_recibo_metodo_pago (historial_recibo_id, metodo_pago_id)
SELECT id, metodo_pago_id
FROM   public.historial_recibo
WHERE  metodo_pago_id IS NOT NULL;

-- ------------------------------------------------------------
-- 3. Migrar desde edicion_recibo → edicion_recibo_metodo_pago
-- ------------------------------------------------------------
INSERT INTO public.edicion_recibo_metodo_pago (edicion_recibo_id, metodo_pago_id)
SELECT id, metodo_pago_id
FROM   public.edicion_recibo
WHERE  metodo_pago_id IS NOT NULL;

-- Verificación post-migración
SELECT 'recibo_metodo_pago'          AS tabla, COUNT(*) AS registros FROM public.recibo_metodo_pago
UNION ALL
SELECT 'historial_recibo_metodo_pago',          COUNT(*)             FROM public.historial_recibo_metodo_pago
UNION ALL
SELECT 'edicion_recibo_metodo_pago',             COUNT(*)            FROM public.edicion_recibo_metodo_pago;
