-- Auto-generated SQL script #202610021127
-- Clave cartera-maxima (referencia visual y alertas del KPI Cartera).
-- Idempotente: no pisa value ni leyenda si la key ya existe.
-- Destino: controlneg_rmx_db_v02.

INSERT INTO public.configuracion_app ("key", value, leyenda)
SELECT
    'cartera-maxima',
    '2000000',
    'El valor maximo de cartera, valor usado para gestion visual y alertas'
WHERE NOT EXISTS (
    SELECT 1 FROM public.configuracion_app c WHERE c.key = 'cartera-maxima'
);

SELECT setval(
    'configuracion_app_id_seq',
    GREATEST((SELECT MAX(id) FROM configuracion_app), 1)
);
