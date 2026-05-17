-- ============================================================
-- MIGRACIÓN: Soporte múltiples métodos de pago por venta
-- Archivo  : nuevastablas-metodo-pago.sql
-- Descripción: Crea las tablas de relación 1-N para metodo_pago
--              en recibo, historial_recibo y edicion_recibo.
--              Las columnas metodo_pago_id originales se conservan
--              por compatibilidad con producción.
-- ============================================================

-- ------------------------------------------------------------
-- Tabla 1: recibo_metodo_pago
-- ------------------------------------------------------------
CREATE TABLE public.recibo_metodo_pago (
    id            bigserial NOT NULL,
    recibo_id     bigint    NOT NULL,
    metodo_pago_id bigint   NOT NULL,
    CONSTRAINT recibo_metodo_pago_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_rmp_recibo_id       ON public.recibo_metodo_pago (recibo_id);
CREATE INDEX idx_rmp_metodo_pago_id  ON public.recibo_metodo_pago (metodo_pago_id);

-- ------------------------------------------------------------
-- Tabla 2: historial_recibo_metodo_pago
-- ------------------------------------------------------------
CREATE TABLE public.historial_recibo_metodo_pago (
    id                   bigserial NOT NULL,
    historial_recibo_id  bigint    NOT NULL,
    metodo_pago_id       bigint    NOT NULL,
    CONSTRAINT historial_recibo_metodo_pago_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_hrmp_historial_recibo_id ON public.historial_recibo_metodo_pago (historial_recibo_id);
CREATE INDEX idx_hrmp_metodo_pago_id      ON public.historial_recibo_metodo_pago (metodo_pago_id);

-- ------------------------------------------------------------
-- Tabla 3: edicion_recibo_metodo_pago
-- ------------------------------------------------------------
CREATE TABLE public.edicion_recibo_metodo_pago (
    id               bigserial NOT NULL,
    edicion_recibo_id bigint   NOT NULL,
    metodo_pago_id   bigint    NOT NULL,
    CONSTRAINT edicion_recibo_metodo_pago_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_ermp_edicion_recibo_id ON public.edicion_recibo_metodo_pago (edicion_recibo_id);
CREATE INDEX idx_ermp_metodo_pago_id    ON public.edicion_recibo_metodo_pago (metodo_pago_id);
