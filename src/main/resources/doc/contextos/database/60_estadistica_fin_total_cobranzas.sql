-- Cobranzas CxC en resumen económico (distintas de ventas POS del corte).
ALTER TABLE estadistica_fin
    ADD COLUMN IF NOT EXISTS total_cobranzas NUMERIC(15, 2);

COMMENT ON COLUMN estadistica_fin.total_cobranzas IS
    'Suma ENTRADA_COBRANZA del periodo. No incluye base de caja ni traslados.';
