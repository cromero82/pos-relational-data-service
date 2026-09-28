-- Editar / restaurar un ticket borra historial_recibo.
-- documento_venta.historial_recibo_id era NOT NULL y fk_dv_historial impedía el delete.
-- El comprobante queda (anulado) sin fila de historial. UNIQUE admite varios NULL.

ALTER TABLE documento_venta
    ALTER COLUMN historial_recibo_id DROP NOT NULL;
