-- Sprint B+C: monto recibido del email puede diferir del esperado QR.
ALTER TABLE historial_recibos_electronicos
    ADD COLUMN IF NOT EXISTS monto_recibido NUMERIC(12, 2);

COMMENT ON COLUMN historial_recibos_electronicos.monto_recibido IS
    'Monto del email bancario al confirmar. Null si aún no confirmado o igual implícito al esperado.';
