-- Nombre del cliente/ticket al crear el pendiente QR (panel pagos electrónicos)
ALTER TABLE historial_recibos_electronicos
    ADD COLUMN IF NOT EXISTS nombre_cliente VARCHAR(200);

COMMENT ON COLUMN historial_recibos_electronicos.nombre_cliente IS
    'Cliente identificado del ticket (o nombre de pestaña) al momento del pago QR.';
