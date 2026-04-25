# Migracion a InfluxDB - guia de pruebas

Cambios introducidos en el ms negocio para reemplazar las tablas postgres
`app_log` y `reporte_frontend` por escrituras a InfluxDB 3 Core.

## Lo que cambio

| Antes | Ahora |
|---|---|
| `DbAppender` -> INSERT a `app_log` (postgres) | `InfluxLogAppender` -> POST line protocol a InfluxDB measurement `backend_log` |
| `POST /reporte-frontend` -> JPA save en `reporte_frontend` | mismo endpoint, mismo body, ahora escribe a InfluxDB measurement `frontend_error` |
| Sincrono | Asincrono (cola en memoria + batch + spool a disco si Influx esta caido) |

Archivos huerfanos (vaciados, listos para borrar fisicamente):
- `logging/DbAppender.java`
- `logging/Log4jDataSourceConfig.java`
- `entities/ReporteFrontend.java`
- `repositories/ReporteFrontendRepository.java`

Borrarlos con CMD parado en la raiz del repo:
```
del src\main\java\com\infinitesoft\pos_relational_data_service\logging\DbAppender.java
del src\main\java\com\infinitesoft\pos_relational_data_service\logging\Log4jDataSourceConfig.java
del src\main\java\com\infinitesoft\pos_relational_data_service\entities\ReporteFrontend.java
del src\main\java\com\infinitesoft\pos_relational_data_service\repositories\ReporteFrontendRepository.java
```

## Pre-requisitos

1. InfluxDB 3 Core arriba en `127.0.0.1:8181` (ver
   `C:\dev\repos\logs-infinito\scripts\start-influxdb.ps1`).
2. (Opcional) Verificar:
   ```
   curl http://127.0.0.1:8181/health
   ```

## Plan de pruebas

### 1. Compilar el ms negocio
```
cd C:\dev\repos\pos-relational-data-service
mvnw clean package -DskipTests
```
Esperado: `BUILD SUCCESS`.

### 2. Arrancar el ms negocio
```
mvnw spring-boot:run
```
En el log de arranque deberias ver:
```
... InfluxWriter started: url=http://127.0.0.1:8181 db=infinito_logs spool=... batch=100 flush=2000ms
... InfluxDB database 'infinito_logs' created with retention 90d
```
(o `'infinito_logs' already exists` si ya existia)

### 3. Verificar que los logs llegan
Hacer un par de requests cualquiera al ms negocio (ej: `GET /actuator/health`).
Despues consultar InfluxDB:
```
curl -G "http://127.0.0.1:8181/api/v3/query_sql" ^
  --data-urlencode "db=infinito_logs" ^
  --data-urlencode "q=SELECT time, level, logger, message FROM backend_log ORDER BY time DESC LIMIT 10" ^
  --data-urlencode "format=jsonl"
```
Esperado: lineas JSON con los logs recientes.

### 4. Verificar el endpoint /reporte-frontend
```
curl -X POST http://localhost:8088/reporte-frontend ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -d "{ \"url\": \"/apps/tickets\", \"actividadReciente\": \"prueba\", \"error\": \"TypeError: x is undefined\" }"
```
Esperado: HTTP 202.

Despues:
```
curl -G "http://127.0.0.1:8181/api/v3/query_sql" ^
  --data-urlencode "db=infinito_logs" ^
  --data-urlencode "q=SELECT time, url, error_type, error FROM frontend_error ORDER BY time DESC LIMIT 5" ^
  --data-urlencode "format=jsonl"
```
Esperado: la linea recien insertada.

### 5. Probar resiliencia (Influx caido)
1. Para Influx: `taskkill /F /IM influxdb3.exe`
2. Genera unos cuantos logs en el ms negocio (cualquier request).
3. Mira la carpeta `monitor-spool/` (en el cwd del ms negocio):
   `dir monitor-spool`
   Veras archivos `.lp` con timestamp.
4. Re-arranca Influx: `.\scripts\start-influxdb.ps1`
5. En 30s (o lo que diga `monitor.influx.retry-interval-ms`) los archivos
   .lp deberian desaparecer y los datos aparecer en Influx.

## Properties relevantes (`application.properties`)

```
monitor.influx.url=http://127.0.0.1:8181
monitor.influx.database=infinito_logs
monitor.influx.retention=90d
monitor.influx.batch-size=100
monitor.influx.flush-interval-ms=2000
monitor.influx.queue-capacity=10000
monitor.influx.spool-dir=./monitor-spool
monitor.influx.retry-interval-ms=30000
monitor.influx.http-timeout-ms=5000
monitor.influx.auth-header=
```

`logging.db.level=INFO` sigue controlando el nivel minimo que se manda a Influx
(igual que antes).

## Despues de validar todo

Ejecutar el script SQL para droppear las tablas viejas:
```
psql -h localhost -U romax-admin -d controlneg_rmx_db -f src\main\resources\doc\drop-tablas-monitoreo.sql
```
