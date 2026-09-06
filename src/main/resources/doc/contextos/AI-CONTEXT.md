# AI Context - pos-relational-data-service (ms negocio)

> **2026-08:** Contexto general del POS → [`AI-ONBOARDING-basic.md`](./AI-ONBOARDING-basic.md).  
> Finanzas / movimientos / orígenes / reset → [`AI-HANDOFF-FINANZAS-2026-08.md`](./AI-HANDOFF-FINANZAS-2026-08.md).  
> Simular días / cortes sandbox (**pendiente**) → [`AI-HANDOFF-SANDBOX-SIMULAR-DIA-2026-09.md`](./AI-HANDOFF-SANDBOX-SIMULAR-DIA-2026-09.md).  
> Monitor / HAR del FE (Angular) → [`AI-HANDOFF-MONITOR-2026-08.md`](./AI-HANDOFF-MONITOR-2026-08.md).  
> Este archivo queda enfocado en **monitoreo backend → InfluxDB** (no confundir con el botón **Monitor** del FE ni con finanzas).

> Antes de tocar logs: si existe el repo `logs-infinito`, leer también su
> onboarding. Aquí solo va lo específico de este microservicio respecto a Influx.

## Que es

Microservicio Spring Boot 2.7 / Java 11 / log4j2 / Postgres + JPA. Es el
"backend de negocio" del POS: productos, tickets, ventas, recibos,
detalles de recibo, historico, reportes. Puerto **8088**.

## Cambio reciente que hay que conocer (logs-infinito)

Las tablas postgres `app_log` (logs estructurados) y `reporte_frontend`
(errores del frontend Angular) **dejaron de usarse**. Ahora todo va a
InfluxDB 3 Core (corre en `127.0.0.1:8181`, database `infinito_logs`).

- Logs del backend -> measurement `backend_log`
- Errores frontend (POST `/reporte-frontend`) -> measurement `frontend_error`

El contrato HTTP del endpoint `/reporte-frontend` **NO cambio** (mismo
JSON de request, mismo `202 Accepted`). El frontend Angular no necesita
cambios.

## Componentes nuevos que hay que conocer

Paquete `com.infinitesoft.pos_relational_data_service.monitoring/`:
- `InfluxProperties` - bind de `monitor.influx.*` desde
  `application.properties`.
- `InfluxConfig` - bean WebClient con timeouts.
- `InfluxStaticHolder` - puente para que el plugin log4j2 alcance al
  bean Spring (mismo patron que el viejo `Log4jDataSourceConfig`).
- `LineProtocol` - builder con escape de tags/fields.
- `InfluxWriter` - **el corazon**. Cola en memoria + worker thread +
  spool a disco (`./monitor-spool/*.lp`) + retry job. NO bloquea ni
  lanza excepciones desde el productor.
- `InfluxBootstrap` - `CommandLineRunner` que crea la database
  `infinito_logs` con retencion 90d al startup (idempotente).

Paquete `logging/`:
- `InfluxLogAppender` - plugin log4j2. Reemplaza al viejo `DbAppender`.
- `DbAppender.java`, `Log4jDataSourceConfig.java` quedaron como archivos
  vacios y deprecated; se pueden borrar fisicamente.

DTO nuevo: `dto/ReporteFrontendRequest` (sin JPA).

Modificados: `application.properties`, `log4j2-spring.xml`,
`controllers/ReporteFrontendController`,
`services/impl/ReporteFrontendServiceImpl`, `.gitignore`.

Vacios y borrables: `entities/ReporteFrontend.java`,
`repositories/ReporteFrontendRepository.java`.

## Properties relevantes

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

`logging.db.level=INFO` controla el nivel minimo que se manda a Influx
(igual que antes).

## Gotcha critico para nuevos fields

InfluxDB 3 tiene **schema dinamico**: una columna solo existe si se ha
escrito alguna vez. Si en cualquier momento se anade un nuevo field
opcional, hay que escribirlo SIEMPRE (con `""` cuando no aplique) para
que el schema lo registre y los queries de Grafana no revienten con
"no field named ...". Vivido con la columna `exception`.

## Doc local

- `src/main/resources/doc/migracion-influxdb.md` - plan de pruebas
  paso a paso.
- `src/main/resources/doc/drop-tablas-monitoreo.sql` - script para
  droppear las tablas postgres viejas cuando se valide todo.

## Pre-requisitos para arrancar y probar

1. InfluxDB arriba (puerto 8181). Lo arranca el ms negocio NO; lo
   arranca el repo `logs-infinito`:
   ```
   powershell -ExecutionPolicy Bypass -File C:\dev\repos\logs-infinito\scripts\start-influx.ps1
   ```
   o desde el launcher (boton "Iniciar" en la fila InfluxDB).

2. Postgres arriba (los datos del POS siguen ahi).

3. `mvnw clean package -DskipTests && mvnw spring-boot:run`.
