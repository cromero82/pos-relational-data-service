SET search_path TO security;


SELECT
    cols.table_name,
    cols.column_name,
    cols.data_type,
    COALESCE(cols.character_maximum_length, cols.numeric_precision) AS longitud,
    cols.is_nullable,
    (SELECT 'SÍ'
     FROM information_schema.key_column_usage kcu
              JOIN information_schema.table_constraints tc
                   ON kcu.constraint_name = tc.constraint_name
     WHERE kcu.table_schema = cols.table_schema -- Importante: mismo esquema
       AND kcu.table_name = cols.table_name
       AND kcu.column_name = cols.column_name
       AND tc.constraint_type = 'FOREIGN KEY'
                                                                       LIMIT 1) AS es_fk
FROM information_schema.columns cols
WHERE cols.table_schema = 'security'  -- <--- CAMBIA ESTO AQUÍ
ORDER BY cols.table_name, cols.ordinal_position;