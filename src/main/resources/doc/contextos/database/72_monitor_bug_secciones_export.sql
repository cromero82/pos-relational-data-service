-- monitor-bug.secciones pasa a rutas del JSON completo que se copia al portapapeles.
-- Ejemplo: requests.requestHeaders.Accept. Idempotente.
-- Destino: controlneg_rmx_db_v02.

UPDATE configuracion_app
SET value = '{"exportedAt":{"trazable":true},"user":{"trazable":false},"route":{"trazable":true},"userAgent":{"trazable":false},"requests.id":{"trazable":true},"requests.timestamp":{"trazable":false},"requests.method":{"trazable":true},"requests.requestHeaders":{"trazable":false},"requests.requestHeaders.Accept":{"trazable":false},"requests.requestHeaders.Authorization":{"trazable":false},"requests.requestHeaders.Content-Type":{"trazable":false},"requests.requestParams":{"trazable":false},"requests.requestParams.query":{"trazable":false},"requests.requestBody":{"trazable":true},"requests.responseHeaders":{"trazable":false},"requests.responseHeaders.content-type":{"trazable":false},"requests.responseBody":{"trazable":true},"requests.responseStatusText":{"trazable":false}}'
WHERE key = 'monitor-bug.secciones';

INSERT INTO configuracion_app (key, value, leyenda)
SELECT
    'monitor-bug.secciones',
    '{"exportedAt":{"trazable":true},"user":{"trazable":false},"route":{"trazable":true},"userAgent":{"trazable":false},"requests.id":{"trazable":true},"requests.timestamp":{"trazable":false},"requests.method":{"trazable":true},"requests.requestHeaders":{"trazable":false},"requests.requestHeaders.Accept":{"trazable":false},"requests.requestHeaders.Authorization":{"trazable":false},"requests.requestHeaders.Content-Type":{"trazable":false},"requests.requestParams":{"trazable":false},"requests.requestParams.query":{"trazable":false},"requests.requestBody":{"trazable":true},"requests.responseHeaders":{"trazable":false},"requests.responseHeaders.content-type":{"trazable":false},"requests.responseBody":{"trazable":true},"requests.responseStatusText":{"trazable":false}}',
    'En seccion de Monitor, las secciones marcadas con check seran almacenadas en registros de traza de pruebas'
WHERE NOT EXISTS (
    SELECT 1 FROM configuracion_app c WHERE c.key = 'monitor-bug.secciones'
);

SELECT setval(
    'configuracion_app_id_seq',
    GREATEST((SELECT MAX(id) FROM configuracion_app), 1)
);
