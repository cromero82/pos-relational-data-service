SELECT
    id,
    codigo_barras AS valor_que_pasa_a_nombre,
    nombre        AS nombre_actual
FROM producto
WHERE (nombre IS NULL OR nombre = '')
  AND codigo_barras IS NOT NULL
  AND codigo_barras !~ '^[0-9\-]+$'
ORDER BY id;

-- APLICAR CAMBIOS
BEGIN;

UPDATE producto
SET
    nombre        = codigo_barras,
    codigo_barras = NULL
WHERE (nombre IS NULL OR nombre = '')
  AND codigo_barras IS NOT NULL
  AND codigo_barras !~ '^[0-9\-]+$';

-- Verificación: debería mostrar 0 filas tras el UPDATE
SELECT id, nombre, codigo_barras
FROM producto
WHERE (nombre IS NULL OR nombre = '')
  AND codigo_barras IS NOT NULL
  AND codigo_barras !~ '^[0-9\-]+$';

COMMIT;
