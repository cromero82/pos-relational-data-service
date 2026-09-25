-- Sprint: SPLIT de corte de ventas (dividir un corte mal generado en N cortes correctos)
--
-- Mecanismo economico: reverso del corte original + asiento puente (AJUSTE_PUENTE_SPLIT,
-- no contabilizable como ingreso) + re-corte por particion, todo en una sola transaccion del
-- servicio (ver CorteVentaService.dividirCorte). No hay columnas de "supersede": los
-- movimientos originales se reversan (REVERSO_ENTRADA_VENTA / REVERSO_TRASLADO), no se ocultan.
--
-- N=1 (misma particion que el rango original) = modo editor: no reestructura ledger, pero SI
-- crea corte_venta_correccion (tipo EDICION) con motivo obligatorio, para trazabilidad; el
-- ajuste de campos (desfases, etc.) sigue delegado al flujo de revision existente
-- (finalizarRevision). motivo es NOT NULL en toda correccion (SPLIT o EDICION).

ALTER TABLE corte_venta DROP CONSTRAINT ck_corte_venta_estado;
ALTER TABLE corte_venta ADD CONSTRAINT ck_corte_venta_estado
    CHECK (estado IN ('creada', 'revisada', 'eliminado', 'dividido'));

-- Corte generado por un split: apunta al evento de correccion que lo creo.
ALTER TABLE corte_venta ADD COLUMN IF NOT EXISTS origen_split_id BIGINT NULL;

-- Log de correccion (evento de negocio): corte original -> N cortes nuevos.
CREATE TABLE IF NOT EXISTS corte_venta_correccion (
    id BIGSERIAL PRIMARY KEY,
    corte_original_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'creado',
    motivo VARCHAR(500) NOT NULL,
    usuario_id VARCHAR(36) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    total_ventas_original NUMERIC(14, 2) NOT NULL,
    rango_ini_original TIMESTAMP NOT NULL,
    rango_fin_original TIMESTAMP NOT NULL,
    CONSTRAINT fk_corte_venta_correccion_original
        FOREIGN KEY (corte_original_id) REFERENCES corte_venta(id),
    CONSTRAINT ck_corte_venta_correccion_tipo
        CHECK (tipo IN ('SPLIT', 'EDICION')),
    CONSTRAINT ck_corte_venta_correccion_estado
        CHECK (estado IN ('creado')),
    CONSTRAINT ck_corte_venta_correccion_motivo
        CHECK (length(trim(motivo)) > 0)
);

-- Detalle: que cortes nuevos nacieron de la correccion, en que rango y orden.
CREATE TABLE IF NOT EXISTS corte_venta_correccion_detalle (
    id BIGSERIAL PRIMARY KEY,
    correccion_id BIGINT NOT NULL,
    corte_nuevo_id BIGINT NOT NULL,
    fecha_desde TIMESTAMP NOT NULL,
    fecha_hasta TIMESTAMP NOT NULL,
    orden INTEGER NOT NULL,
    CONSTRAINT fk_corte_venta_correccion_detalle_correccion
        FOREIGN KEY (correccion_id) REFERENCES corte_venta_correccion(id),
    CONSTRAINT fk_corte_venta_correccion_detalle_corte_nuevo
        FOREIGN KEY (corte_nuevo_id) REFERENCES corte_venta(id)
);

ALTER TABLE corte_venta ADD CONSTRAINT fk_corte_venta_origen_split
    FOREIGN KEY (origen_split_id) REFERENCES corte_venta_correccion(id);

CREATE INDEX IF NOT EXISTS idx_corte_venta_correccion_original
    ON corte_venta_correccion (corte_original_id);

CREATE INDEX IF NOT EXISTS idx_corte_venta_correccion_detalle_correccion
    ON corte_venta_correccion_detalle (correccion_id, orden);

CREATE INDEX IF NOT EXISTS idx_corte_venta_origen_split
    ON corte_venta (origen_split_id)
    WHERE origen_split_id IS NOT NULL;
