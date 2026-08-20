# AI Onboarding — Sistema POS Infinito (actual)

**Fecha de corte:** 2026-08-05 (rev. tarde: egreso sin MP + reset v2)  
**Contexto / fork:** trabajo de **movimientos / orígenes de fondos / cierres**  
**Branches:** BE `dian-v2` · FE `dian-version`  
**Propósito:** punto de entrada para cualquier IA o desarrollador que retome el trabajo.  
**Rutas locales (macOS):** `/Users/carlosromero/Documents/dev/repos/...`

> Docs más viejos (`AI-ONBOARDING-v2.md`, `AI-CONTEXT.md` Influx, `info.md`) siguen útiles por dominio.  
> **Fuente de verdad operativa:** este archivo + handoffs `AI-HANDOFF-*-2026-08.md`.

---

## 0. Índice de contextos en esta carpeta

| Archivo | Usar cuando… |
|---------|----------------|
| **Este** (`AI-ONBOARDING-basic.md`) | Arranque de cualquier chat / mapa del sistema |
| [`AI-HANDOFF-FINANZAS-2026-08.md`](./AI-HANDOFF-FINANZAS-2026-08.md) | Orígenes, ledger, egresos, cierre, distribución, base inicial, **reset** |
| [`AI-HANDOFF-MONITOR-2026-08.md`](./AI-HANDOFF-MONITOR-2026-08.md) | Botón **Monitor** (bug-reporter), HAR JSON, Ver detalle |
| [`database/README-SPRINTS.md`](./database/README-SPRINTS.md) | Orden SQL / migrate prod→dian-v2 |
| [`AI-CONTEXT.md`](./AI-CONTEXT.md) | Logs BE → InfluxDB (no finanzas; no confundir con Monitor FE) |
| [`contexto-legal-pos-responsabilidad.md`](./contexto-legal-pos-responsabilidad.md) | Diseño legal / responsabilidad POS CO |
| [`AI-ONBOARDING-v2.md`](./AI-ONBOARDING-v2.md) | Histórico monitoreo (abr 2026); no verdad financiera |
| [`temporal/prompts.prompt`](./temporal/prompts.prompt) | Notas de planeación (multi-caja, permisos…) |

**Fuera de este repo (operativo):**

| Path | Uso |
|------|-----|
| `prompts-general-pos/RESET-TABLAS-FINANCIERAS-TRANSACCIONALES.md` | Reset prueba + gotchas DBeaver |
| `prompts-general-pos/reset-tablas-financieras-transaccionales-v2.sql` | SQL canónico truncate |
| `prompts-general-pos/MIGRATE-PROD-TO-DIAN-V2.md` | Migración schema |
| `prompts-general-pos/MULTIPAGO-MEDIOS-POR-TICKET.md` | Cobro 2–3 medios / corte por líneas |
| `prompts-general-pos/GLOSARIO-NUCLEO-FINANCIERO.md` | Ventas / Esperado / Contado / Diferencia / CxC |
| `infinito-ai-front/.cursor/rules/movimientos-almacen/ONBOARDING-FINANZAS-ORIGENES-CIERRES.md` | Onboarding FE (julio; delta → handoff Ago) |
| `infinito-ai-front/.cursor/rules/movimientos-almacen/POS-PLAN-MAESTRO.md` | Roadmap |

---

## 1. TL;DR

POS para tiendas colombianas, despliegue **local**.

| Pieza | Repo | Branch | Puerto |
|-------|------|--------|--------|
| Backend negocio | `pos-relational-data-service` | `dian-v2` | `:8088` |
| Frontend Angular | `infinito-ai-front` | `dian-version` | `:4200` |
| Auth / usuarios | `infinito-security` | (entorno) | `:8081` |
| PostgreSQL | `controlneg_rmx_db` | — | `:5432` |
| Prompts / reset / HARs | `prompts-general-pos` | — | — |

Stack BE: Java 11, Spring Boot 2.7.18, JPA, `ddl-auto=none` → **SQL manual**.  
Stack FE: Angular 21.x (paquete histórico Vex).

**Issues:** casi no hay tracker GitHub de producto; backlog en prompts + planes `movimientos-almacen` + § pendientes del handoff finanzas.

---

## 2. Qué leer según la tarea

| Tarea | Leer primero |
|-------|----------------|
| Orígenes / ledger / egresos / cierre / base / **reset** | [`AI-HANDOFF-FINANZAS-2026-08.md`](./AI-HANDOFF-FINANZAS-2026-08.md) |
| Monitor / HAR / Ver detalle | [`AI-HANDOFF-MONITOR-2026-08.md`](./AI-HANDOFF-MONITOR-2026-08.md) |
| Orden SQL migrate | [`database/README-SPRINTS.md`](./database/README-SPRINTS.md) |
| Vaciar datos de prueba | `prompts-general-pos/RESET-…md` + script **v2** |
| Logs → Influx | [`AI-CONTEXT.md`](./AI-CONTEXT.md) |
| Legal | [`contexto-legal-pos-responsabilidad.md`](./contexto-legal-pos-responsabilidad.md) |

---

## 3. Dominio financiero (estado real Ago 2026)

```text
metodo_pago              = tender del ticket (efectivo, Nequi, QR…)
historial_recibo_pago    = líneas de cobro multipago (SUM = total; corte agrega desde aquí)
origen_fondos            = cuenta/wallet donde vive el dinero  ← PRIMA en egresos
movimiento_origen_fondos = ledger (saldo = SUM(impacto))
```

**Ya implementado (rama movimientos):**

- Árbol OF + DnD traslados (hijo → padre/hermanos) + footer tips
- Ledger: entrada, préstamo, traslado, egreso, ajustes, entradas venta cierre, distribución…
- Cierre: base / ventas / egresos (− UI) / movimientos / neto / físico / desfase
- Watermarks recibo + MOF; `consultar-rango` con actividad venta **o** movimiento
- Distribución efectivo; base inicial (modal login admin)
- `id_referencia` + modal «Ver» (no en DISTRIBUCION)
- Total parcial en traslados; fila movimiento seleccionada persistente
- Egreso **sin** método de pago obligatorio (Caja Menor / General)
- Reset prueba v2 (egreso + stats + flujo_dinero + aserción BASE_INICIAL)

**Pendiente producto:** permisos estricto; multi-caja; emisión XML DIAN (`PaymentMeans`); idempotencia edit egreso; anulación ticket↔ledger; visibilidad en cierre de egresos sin MP.
**Hecho (ago 2026):** multipago operativo — ver `prompts-general-pos/MULTIPAGO-MEDIOS-POR-TICKET.md`.

Detalle: handoff finanzas.

---

## 4. Monitor (FE) vs Influx (BE)

| Nombre | Qué es |
|--------|--------|
| **Monitor** (UI) | `infinito-ai-front` `bug-reporter` — captura HAR HTTP para debug |
| **AI-CONTEXT Influx** | Logs estructurados BE → InfluxDB `:8181` |

No confundir. Detalle Monitor → handoff Monitor.

---

## 5. Runtime y seguridad operativa

```bash
lsof -iTCP:8088 -sTCP:LISTEN   # BE
lsof -iTCP:4200 -sTCP:LISTEN   # FE
lsof -iTCP:8081 -sTCP:LISTEN   # auth
```

- Segundo `mvn spring-boot:run` → PortInUse si IntelliJ ya levantó `:8088`.
- Exit **137/143** = kill/SIGTERM, no compile fail.
- **No** `git reset --hard` / force-push main / commit sin petición.
- Reset prueba: **siempre** script v2 (no truncate parcial). DBeaver: Auto-commit ON + Execute Script.

```bash
cd .../doc/contextos/database && ./apply-migrate-prod-to-dian-v2.sh
# Reset datos:
bash .../prompts-general-pos/apply-reset-tablas-financieras-transaccionales.sh
# luego logout + login admin → modal base inicial
```

---

## 6. Controllers / rutas FE clave

**BE:** `OrigenFondosController`, `MovimientoOrigenFondosController`, `MotivoMovimientoController`, `EgresoController`, `CorteVentaController`, `EstablecimientoController`, + ventas/inventario.

**FE:**

```text
/apps/financiero/origenes-fondos
/apps/financiero/egresos              (?nuevo=1 → crear)
/apps/financiero/ingresos             → cierre + revisión + distribución + base
/apps/ventas
```

---

## 7. Commits de referencia

| Repo | Commit | Nota |
|------|--------|------|
| BE `dian-v2` | `b5d23e8` | `idReferencia` |
| BE | `e7b8563` | movimientos / egresos / orígenes |
| FE `dian-version` | `f51a8b2` | orígenes + Monitor Ver detalle |
| FE | `bc44b71` | orígenes, establecimiento, HARs |

Working tree puede tener ajustes no commiteados (egreso MP nullable, polish UI, docs): `git status`.

---

## 8. Cómo continuar en un chat nuevo

1. Leer **este** archivo.
2. Finanzas/cierres/reset → `AI-HANDOFF-FINANZAS-2026-08.md`.
3. Monitor/HAR → `AI-HANDOFF-MONITOR-2026-08.md`.
4. Confirmar branch + puertos + `git status` BE/FE.
5. Pedir JSON Monitor o doble clic en orígenes si hay que auditar.
6. Si “no pide base inicial”: verificar `BASE_INICIAL=0` y cortes=0 **committed**; logout+login admin.

Última actualización: **2026-08-05 (tarde)**.
