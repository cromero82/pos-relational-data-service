-- Comentario libre del ticket (panel Crédito / Observación en Tickets).
ALTER TABLE ticket
    ADD COLUMN IF NOT EXISTS observaciones TEXT;
