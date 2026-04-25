-- =============================================================================
-- drop-tablas-monitoreo.sql
-- Elimina las tablas postgres que ya NO se usan despues de la migracion a
-- InfluxDB (proyecto logs-infinito).
--
-- EJECUTAR MANUALMENTE cuando hayas verificado que:
--   1. La aplicacion arranca con InfluxDB y los logs llegan a Influx.
--   2. El endpoint POST /reporte-frontend tambien escribe a Influx
--      (measurement frontend_error).
--   3. Las tablas viejas ya no contienen datos importantes que necesites
--      conservar (o ya hiciste backup).
--
-- Como ejecutarlo:
--   psql -h localhost -U romax-admin -d controlneg_rmx_db -f drop-tablas-monitoreo.sql
-- =============================================================================

-- (Opcional) Backup rapido a CSV antes de borrar:
-- \copy app_log         TO 'C:/temp/app_log_backup.csv'         CSV HEADER;
-- \copy reporte_frontend TO 'C:/temp/reporte_frontend_backup.csv' CSV HEADER;

BEGIN;

-- Tabla de logs estructurados del backend (antes alimentada por DbAppender)
DROP TABLE IF EXISTS app_log CASCADE;

-- Tabla de reportes de errores del frontend (antes alimentada por POST /reporte-frontend)
DROP TABLE IF EXISTS reporte_frontend CASCADE;

COMMIT;

-- Verificacion:
SELECT 'app_log existe?'         AS chequeo, EXISTS(SELECT 1 FROM information_schema.tables
       WHERE table_schema='public' AND table_name='app_log') AS resultado
UNION ALL
SELECT 'reporte_frontend existe?', EXISTS(SELECT 1 FROM information_schema.tables
       WHERE table_schema='public' AND table_name='reporte_frontend');
