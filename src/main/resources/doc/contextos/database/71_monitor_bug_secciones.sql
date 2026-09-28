-- Secciones del Monitor (HAR) que se almacenan en la traza.
-- Idempotente: no pisa value ni leyenda si la key ya existe.
-- Destino: controlneg_rmx_db_v02.

INSERT INTO configuracion_app (key, value, leyenda)
SELECT
    'monitor-bug.secciones',
    '{"requestHeaders":{"trazable":true},"requestHeaders.Accept":{"trazable":true},"requestHeaders.Authorization":{"trazable":true},"requestHeaders.Content-Type":{"trazable":true},"requestParams":{"trazable":true},"requestParams.query":{"trazable":true},"requestBody":{"trazable":true},"responseHeaders":{"trazable":true},"responseHeaders.content-type":{"trazable":true},"responseBody":{"trazable":true},"responseStatusText":{"trazable":true}}',
    'En seccion de Monitor, las secciones marcadas con check seran almacenadas en registros de traza de pruebas'
WHERE NOT EXISTS (
    SELECT 1 FROM configuracion_app c WHERE c.key = 'monitor-bug.secciones'
);

SELECT setval(
    'configuracion_app_id_seq',
    GREATEST((SELECT MAX(id) FROM configuracion_app), 1)
);
