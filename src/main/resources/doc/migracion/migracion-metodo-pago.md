# Migración: Soporte múltiples métodos de pago por venta

## Contexto
Se amplió el modelo de datos para que un proceso de venta (recibo, historial_recibo, edicion_recibo)
pueda registrar **más de un método de pago**. Las columnas `metodo_pago_id` originales se conservan
por compatibilidad con producción; las nuevas tablas junction implementan la relación 1-N.

---

## Tablas nuevas en base de datos

| Tabla | Propósito |
|---|---|
| `recibo_metodo_pago` | Métodos de pago asociados a un recibo pendiente |
| `historial_recibo_metodo_pago` | Métodos de pago de ventas ya finalizadas |
| `edicion_recibo_metodo_pago` | Métodos de pago de recibos en edición |

**Scripts a ejecutar en orden:**
1. `nuevastablas-metodo-pago.sql` — crea las tres tablas y sus índices
2. `migrar-datos-metodo-pago.sql` — copia los `metodo_pago_id` existentes a las tablas junction

---

## Cambios en contratos de API

### 1. `POST /recibos` — Crear recibo

**Antes:**
```bash
curl -X POST http://localhost:8080/recibos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 1,
    "metodoPagoId": 2,
    "sesionId": 5,
    "total": 15000.00,
    "montoRecibido": 15000.00
  }'
```

**Ahora:**
```bash
curl -X POST http://localhost:8080/recibos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 1,
    "metodoPagoIds": [2, 3],
    "sesionId": 5,
    "total": 15000.00,
    "montoRecibido": 15000.00
  }'
```

> `metodoPagoIds` es una lista. El primer elemento también se persiste en la columna de compatibilidad `metodo_pago_id`.  
> Para un solo método de pago: `"metodoPagoIds": [2]`

---

### 2. `PUT /recibos/{id}` — Actualizar recibo

**Antes:**
```bash
curl -X PUT http://localhost:8080/recibos/10 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 3,
    "metodoPagoId": 2,
    "sesionId": 5,
    "total": 15000.00,
    "montoRecibido": 15000.00
  }'
```

**Ahora:**
```bash
curl -X PUT http://localhost:8080/recibos/10 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 3,
    "metodoPagoIds": [2, 3],
    "sesionId": 5,
    "total": 15000.00,
    "montoRecibido": 15000.00
  }'
```

---

### 3. `GET /recibos/{id}` — Consultar recibo por ID

**Respuesta antes:**
```json
{
  "id": 10,
  "clienteId": 1,
  "estadoId": 3,
  "estado": "PAGADO",
  "metodoPagoId": 2,
  "sesionId": 5,
  "total": 15000.00,
  "montoRecibido": 15000.00
}
```

**Respuesta ahora:**
```json
{
  "id": 10,
  "clienteId": 1,
  "estadoId": 3,
  "estado": "PAGADO",
  "metodoPagoIds": [2, 3],
  "sesionId": 5,
  "total": 15000.00,
  "montoRecibido": 15000.00
}
```

---

### 4. `POST /historial-recibos` — Crear historial recibo

**Antes:**
```bash
curl -X POST http://localhost:8080/historial-recibos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 3,
    "metodoPagoId": 2,
    "sesionId": 5,
    "total": 12000.00,
    "montoRecibido": 12000.00
  }'
```

**Ahora:**
```bash
curl -X POST http://localhost:8080/historial-recibos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 3,
    "metodoPagoId": 2,
    "metodoPagoIds": [2, 3],
    "sesionId": 5,
    "total": 12000.00,
    "montoRecibido": 12000.00
  }'
```

> `metodoPagoId` se conserva por compatibilidad. Si se envía `metodoPagoIds`, este tiene precedencia.  
> El endpoint `POST /historial-recibos/addquickRecibo` sigue el mismo esquema.

---

### 5. `PUT /historial-recibos/{id}` — Actualizar historial recibo

**Antes:**
```bash
curl -X PUT http://localhost:8080/historial-recibos/7?sesionId=5 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 3,
    "metodoPagoId": 2,
    "total": 12000.00,
    "montoRecibido": 12000.00
  }'
```

**Ahora:**
```bash
curl -X PUT http://localhost:8080/historial-recibos/7?sesionId=5 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "estadoId": 3,
    "metodoPagoIds": [2, 3],
    "total": 12000.00,
    "montoRecibido": 12000.00
  }'
```

---

### 6. `GET /historial-recibos` y `GET /historial-recibos/{id}` — Respuesta

**Respuesta antes (fragmento):**
```json
{
  "id": 7,
  "metodoPagoId": 2
}
```

**Respuesta ahora:**
```json
{
  "id": 7,
  "metodoPagoId": 2,
  "metodoPagoIds": [2, 3]
}
```

> `metodoPagoId` sigue retornándose (campo legado). El nuevo campo `metodoPagoIds` es la fuente de verdad.

---

### 7. `POST /edicion-recibos` — Crear edición de recibo

**Antes:**
```bash
curl -X POST http://localhost:8080/edicion-recibos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reciboId": 10,
    "clienteId": 1,
    "estadoId": 4,
    "metodoPagoId": 2,
    "sesionId": 5,
    "total": 15000.00,
    "montoRecibido": 15000.00
  }'
```

**Ahora:**
```bash
curl -X POST http://localhost:8080/edicion-recibos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reciboId": 10,
    "clienteId": 1,
    "estadoId": 4,
    "metodoPagoIds": [2, 3],
    "sesionId": 5,
    "total": 15000.00,
    "montoRecibido": 15000.00
  }'
```

---

### 8. `GET /edicion-recibos/{id}` — Respuesta

**Antes:**
```json
{ "id": 5, "metodoPagoId": 2 }
```

**Ahora:**
```json
{ "id": 5, "metodoPagoId": 2, "metodoPagoIds": [2, 3] }
```

---

### 9. `GET /copias-seguridad/generar-backup` — Backup Excel

El archivo Excel generado ahora incluye tres hojas adicionales:

| Hoja | Columnas |
|---|---|
| `Recibo Metodo Pago` | ID, Recibo ID, Metodo Pago ID |
| `Hist Recibo Metodo Pago` | ID, Historial Recibo ID, Metodo Pago ID |
| `Edicion Recibo Met Pago` | ID, Edicion Recibo ID, Metodo Pago ID |

---

### 10. `POST /copias-seguridad/restaurar-backup` — Restaurar backup

El endpoint acepta los mismos parámetros (`file`, `tipo`). La respuesta ahora incluye contadores para las tablas junction:

```json
{
  "exito": true,
  "reciboMetodoPagoCreados": 120,
  "reciboMetodoPagoActualizados": 0,
  "historialReciboMetodoPagoCreados": 450,
  "historialReciboMetodoPagoActualizados": 0,
  "edicionReciboMetodoPagoCreados": 10,
  "edicionReciboMetodoPagoActualizados": 0
}
```

---

### 11. `POST /corte-ventas` — Consultar rango (`consultarRango`)

El resumen `ventasTipo` en la respuesta de `CorteVentaRangoResponse` ahora se calcula
usando la tabla `historial_recibo_metodo_pago`. Si un recibo tiene múltiples métodos de pago,
su total se atribuye a cada método. Los campos no cambian:

```json
{
  "ventasTipo": [
    { "metodoPagoId": 1, "totalSistema": 80000.00 },
    { "metodoPagoId": 2, "totalSistema": 35000.00 }
  ]
}
```

---

## Compatibilidad hacia atrás

- Las columnas `metodo_pago_id` en `recibo`, `historial_recibo` y `edicion_recibo` **se conservan** intactas.
- El backend sigue leyendo y escribiendo ese campo (primer elemento de la lista).
- Clientes que envíen el campo antiguo `metodoPagoId` en `HistorialRecibo` seguirán funcionando; el servicio detecta el campo y lo convierte a lista de un elemento automáticamente.
- La tabla `ventas_tipo` **no se modifica** (ya soportaba 1-N por `corte_venta_id`).
