-- Sprint B8 — workflow de cierre, snapshot por fila y revisión administrativa

ALTER TABLE corte_venta
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'creada',
    ADD COLUMN IF NOT EXISTS observacion VARCHAR(200) NULL,
    ADD COLUMN IF NOT EXISTS revisado_por VARCHAR(36) NULL,
    ADD COLUMN IF NOT EXISTS fecha_revision TIMESTAMP NULL;

UPDATE corte_venta
SET estado = 'revisada'
WHERE estado IS NULL OR estado NOT IN ('creada', 'revisada', 'eliminado');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_corte_venta_estado'
    ) THEN
        ALTER TABLE corte_venta
            ADD CONSTRAINT ck_corte_venta_estado
            CHECK (estado IN ('creada', 'revisada', 'eliminado'));
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS corte_venta_detalle (
    id BIGSERIAL PRIMARY KEY,
    corte_venta_id BIGINT NOT NULL,
    metodo_pago_id BIGINT NULL,
    origen_fondos_id INTEGER NULL,
    base NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_ventas_sistema NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_egresos_sistema NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_movimientos_sistema NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_sistema NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total NUMERIC(14, 2) NULL,
    desfase NUMERIC(14, 2) NULL,
    motivo_desfase_id INTEGER NULL,
    modo_captura VARCHAR(25) NOT NULL DEFAULT 'DECLARADO_CAJERO',
    declarado_por VARCHAR(36) NULL,
    revision_estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    revision_comentario VARCHAR(500) NULL,
    ajuste_generado BOOLEAN NOT NULL DEFAULT FALSE,
    orden INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_corte_venta_detalle_corte
        FOREIGN KEY (corte_venta_id) REFERENCES corte_venta(id),
    CONSTRAINT fk_corte_venta_detalle_metodo
        FOREIGN KEY (metodo_pago_id) REFERENCES metodo_pago(id),
    CONSTRAINT fk_corte_venta_detalle_origen
        FOREIGN KEY (origen_fondos_id) REFERENCES origen_fondos(id),
    CONSTRAINT fk_corte_venta_detalle_motivo
        FOREIGN KEY (motivo_desfase_id) REFERENCES motivo_movimiento(id),
    CONSTRAINT ck_corte_detalle_modo
        CHECK (modo_captura IN ('DECLARADO_CAJERO', 'SOLO_VISIBLE', 'DECLARADO_ADMIN')),
    CONSTRAINT ck_corte_detalle_revision
        CHECK (revision_estado IN ('PENDIENTE', 'OK', 'SUGERENCIA')),
    CONSTRAINT ck_corte_detalle_sugerencia
        CHECK (
            revision_estado <> 'SUGERENCIA'
            OR length(trim(COALESCE(revision_comentario, ''))) > 0
        )
);

CREATE INDEX IF NOT EXISTS idx_corte_venta_estado_fecha
    ON corte_venta (estado, fecha_creacion DESC);

CREATE INDEX IF NOT EXISTS idx_corte_detalle_corte
    ON corte_venta_detalle (corte_venta_id, orden, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_corte_detalle_metodo
    ON corte_venta_detalle (corte_venta_id, metodo_pago_id)
    WHERE metodo_pago_id IS NOT NULL;

-- Históricos: conservar el agregado que ya estaba persistido en ventas_tipo.
CREATE TEMP TABLE cortes_legacy_b8 ON COMMIT DROP AS
SELECT cv.id
FROM corte_venta cv
WHERE NOT EXISTS (
    SELECT 1 FROM corte_venta_detalle d WHERE d.corte_venta_id = cv.id
);

INSERT INTO corte_venta_detalle (
    corte_venta_id,
    metodo_pago_id,
    base,
    total_ventas_sistema,
    total_egresos_sistema,
    total_movimientos_sistema,
    total_sistema,
    total,
    desfase,
    motivo_desfase_id,
    modo_captura,
    declarado_por,
    revision_estado,
    ajuste_generado,
    orden
)
SELECT
    vt.corte_venta_id,
    vt.metodo_pago_id,
    0,
    COALESCE(vt.total_ventas_sistema, vt.total_sistema, 0),
    COALESCE(vt.total_egresos_sistema, 0),
    0,
    COALESCE(vt.total_sistema, 0),
    vt.total,
    COALESCE(vt.desfase, vt.total - COALESCE(vt.total_sistema, 0)),
    vt.motivo_desfase_id,
    'DECLARADO_CAJERO',
    cv.usuario_id,
    'OK',
    CASE WHEN COALESCE(vt.desfase, 0) <> 0 THEN TRUE ELSE FALSE END,
    ROW_NUMBER() OVER (PARTITION BY vt.corte_venta_id ORDER BY vt.metodo_pago_id, vt.id)
FROM ventas_tipo vt
JOIN corte_venta cv ON cv.id = vt.corte_venta_id
WHERE NOT EXISTS (
    SELECT 1
    FROM corte_venta_detalle d
    WHERE d.corte_venta_id = vt.corte_venta_id
      AND d.metodo_pago_id = vt.metodo_pago_id
);

-- Los cierres anteriores a B8 no requieren revisión retroactiva.
UPDATE corte_venta cv
SET estado = 'revisada',
    revisado_por = COALESCE(cv.revisado_por, cv.usuario_id),
    fecha_revision = COALESCE(cv.fecha_revision, cv.fecha_creacion)
WHERE cv.id IN (SELECT id FROM cortes_legacy_b8);
