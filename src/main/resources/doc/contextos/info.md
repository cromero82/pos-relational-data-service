# Actualización de Producto - Nuevo Campo Precio Unidad

Se ha agregado el campo `precio_unidad` a la tabla `producto` para permitir el manejo de precios por unidad de medida.

## Cambios Realizados

### 1. Entidad Product.java
Se agregó el campo `precioUnidad` mapeado a la columna `precio_unidad` de la base de datos.

```java
@Column(name = "precio_unidad")
private Double precioUnidad;
```

### 2. ProductServiceImpl.java
Se actualizó el método `update` para procesar el nuevo campo durante la edición de productos.

```java
@Override
@Transactional
public Product update(Long id, Product product) {
    // ...
    existing.setPrecio(product.getPrecio());
    existing.setPrecioCompra(product.getPrecioCompra());
    existing.setPrecioUnidad(product.getPrecioUnidad()); // Nueva línea
    // ...
}
```

## Ejemplo de Uso (JSON)

Para crear o actualizar un producto con el nuevo campo:

**Endpoint:** `POST /products` o `PUT /products/{id}`

**Cuerpo de la petición:**
```json
{
  "barcode": "7701234567890",
  "nombre": "ARROZ BLANCO 1KG",
  "precio": 5000.0,
  "precioCompra": 3800.0,
  "precioUnidad": 5.0,
  "porcentajeGanancia": 32,
  "activate": 1
}
```

---

# Actualización de TicketRecibo con Recibo Padre

Se ha modificado el flujo de creación automática de recibos vinculados a tickets para permitir la asociación con un recibo padre (útil para devoluciones o notas de crédito).

## Cambios Realizados

### 1. TicketReciboController
Se agregó el parámetro opcional `reciboPadreId` al endpoint `getByTicketId`.

```java
@GetMapping("/ticket/{ticketId}")
public ResponseEntity<TicketRecibo> getByTicketId(
        @PathVariable Long ticketId,
        @RequestParam(name = "sessionId", required = true) Long sessionId,
        @RequestParam(name = "reciboPadreId", required = false) Long reciboPadreId) {
    
    TicketRecibo result = service.getOrCreateByTicketId(ticketId, sessionId, reciboPadreId);
    // ...
}
```

### 2. TicketReciboServiceImpl
Se actualizó el método `getOrCreateByTicketId` para recibir este parámetro y asignarlo al construir el `ReciboDto`.

```java
@Override
@Transactional
public TicketRecibo getOrCreateByTicketId(Long ticketId, Long sessionId, Long reciboPadreId) {
    // ... lógica de búsqueda existente ...

    // Creación del nuevo recibo con el campo reciboIdPadre
    ReciboDto nuevoRecibo = ReciboDto.builder()
            .clienteId(clienteId)
            .estadoId(ReciboEstado.PENDIENTE_PAGO.getId())
            .sesionId(sessionId)
            .total(BigDecimal.ZERO)
            .montoRecibido(BigDecimal.ZERO)
            .reciboIdPadre(reciboPadreId) // Asignación del parámetro opcional
            .build();

    Recibo savedRecibo = reciboService.create(nuevoRecibo);
    
    // ... vinculación con el ticket ...
}
```

## Ejemplo de Uso (URL)

Para obtener o crear un recibo asociado a un ticket indicando un recibo padre:

`GET /ticket-recibos/ticket/123?sessionId=1&reciboPadreId=500`

---

# Sistema de Backup y Restauración

Se ha implementado un sistema completo de backup y restauración de datos que permite generar archivos Excel con toda la información del sistema y restaurarla posteriormente.

## Endpoints Disponibles

### 1. Generar Backup

Genera un archivo Excel con todos los datos del sistema (base de datos relacional y servicio de autenticación).

**Endpoint:** `GET /copias-seguridad/generar-backup`

**Headers:**
```
Authorization: Bearer {token}
```

**Respuesta exitosa:**
- Archivo Excel descargable con nombre: `Backup Gestor Market YYYY-MM-DD HH-mm-ss.xlsx`

**Respuesta con error:**
```json
{
  "error": true,
  "message": "Error al generar el backup",
  "detalle": "Descripción específica del error"
}
```

**Ejemplo con cURL:**
```bash
curl --location 'http://localhost:8088/copias-seguridad/generar-backup' \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...' \
  --output "backup.xlsx"
```

### 2. Restaurar Backup

Restaura los datos del sistema desde un archivo Excel previamente generado. Soporta dos modos de restauración.

**Endpoint:** `POST /copias-seguridad/restaurar-backup`

**Parámetros:**
- `file` (multipart/form-data): Archivo Excel del backup
- `tipo` (query param, opcional): Modo de restauración
  - `"incremental"` (por defecto): Solo agrega registros que no existen actualmente
  - `"full-reescritura"`: Borra todos los registros de cada tabla antes de restaurar

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Respuesta exitosa:**
```json
{
  "exito": true,
  "mensaje": "Backup restaurado exitosamente",
  "rolesCreados": 2,
  "rolesActualizados": 0,
  "usuariosCreados": 5,
  "usuariosActualizados": 1,
  "perfilesCreados": 5,
  "perfilesActualizados": 0,
  "clientesCreados": 15,
  "clientesActualizados": 0,
  "productosCreados": 250,
  "productosActualizados": 10,
  "recibosCreados": 100,
  "recibosActualizados": 0
}
```

**Respuesta con error:**
```json
{
  "exito": false,
  "mensaje": "Error al restaurar backup: descripción del error"
}
```

**Ejemplo con cURL (Modo Incremental):**
```bash
curl --location 'http://localhost:8088/copias-seguridad/restaurar-backup?tipo=incremental' \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...' \
  --form 'file=@"backup.xlsx"'
```

**Ejemplo con cURL (Modo Full-Reescritura):**
```bash
curl --location 'http://localhost:8088/copias-seguridad/restaurar-backup?tipo=full-reescritura' \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...' \
  --form 'file=@"backup.xlsx"'
```

**Ejemplo con HTTPie (Modo Incremental):**
```bash
http --form POST 'http://localhost:8088/copias-seguridad/restaurar-backup?tipo=incremental' \
  Authorization:'Bearer eyJhbGciOiJIUzI1NiJ9...' \
  file@backup.xlsx
```

**Ejemplo con JavaScript (Fetch API):**
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]); // fileInput es un <input type="file">

fetch('http://localhost:8088/copias-seguridad/restaurar-backup?tipo=incremental', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9...'
  },
  body: formData
})
.then(response => response.json())
.then(data => {
  console.log('Backup restaurado:', data);
  console.log(`Productos creados: ${data.productosCreados}`);
  console.log(`Recibos creados: ${data.recibosCreados}`);
})
.catch(error => console.error('Error:', error));
```

**Ejemplo con Postman:**
1. Método: `POST`
2. URL: `http://localhost:8088/copias-seguridad/restaurar-backup?tipo=incremental`
3. Headers:
   - `Authorization`: `Bearer eyJhbGciOiJIUzI1NiJ9...`
4. Body:
   - Seleccionar `form-data`
   - Key: `file` (cambiar tipo a "File")
   - Value: Seleccionar el archivo `backup.xlsx`

### 3. Enviar Backup por Correo

Genera un backup y lo envía por correo electrónico al usuario autenticado.

**Endpoint:** `GET /copias-seguridad/exportar-a-correo`

**Headers:**
```
Authorization: Bearer {token}
```

**Respuesta exitosa:**
```json
{
  "message": "Backup enviado correctamente al correo: usuario@ejemplo.com"
}
```

**Ejemplo con cURL:**
```bash
curl --location 'http://localhost:8088/copias-seguridad/exportar-a-correo' \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...'
```

## Detalles Técnicos

### Modo Incremental
- Solo restaura registros que **no existen** en la base de datos actual
- Utiliza el ID de cada registro para determinar si ya existe
- **Ventaja:** No pierde datos actuales, sino que agrega información faltante
- **Uso recomendado:** Para sincronizar datos entre ambientes o recuperar registros eliminados

### Modo Full-Reescritura
- **Borra TODOS** los registros de cada tabla antes de restaurar
- Sobrescribe completamente la base de datos con los datos del backup
- **Ventaja:** Garantiza que la base de datos quede exactamente como estaba en el backup
- **Advertencia:** ⚠️ Se pierden todos los datos actuales que no estén en el backup
- **Uso recomendado:** Para restaurar el sistema a un estado anterior conocido

### Orden de Restauración
El sistema restaura las tablas en el siguiente orden respetando las dependencias:

1. Datos de autenticación (roles, usuarios, perfiles)
2. Tablas sin relaciones (clientes, estados, métodos de pago, configuración, company)
3. Tablas con relaciones (productos, recibos, tickets, egresos, etc.) en orden de dependencias

### Estructura del Excel
El archivo Excel generado contiene una hoja por cada tabla del sistema:
- **Usuarios**, **Roles**, **Usuario Perfiles** (servicio de autenticación)
- **Productos**, **Historial_productos**
- **Clientes**, **Company**
- **Recibo**, **Recibo Detalle**, **Historial recibo**, etc.
- Y todas las demás tablas del sistema

## Casos de Uso

### Caso 1: Backup Periódico Automático
```bash
# Ejecutar diariamente a las 2 AM
0 2 * * * curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8088/copias-seguridad/exportar-a-correo
```

### Caso 2: Migración entre Ambientes
```bash
# 1. Generar backup en producción
curl -H "Authorization: Bearer $TOKEN_PROD" \
  http://prod-server:8088/copias-seguridad/generar-backup \
  -o backup-prod.xlsx

# 2. Restaurar en desarrollo (incremental)
curl -H "Authorization: Bearer $TOKEN_DEV" \
  -F "file=@backup-prod.xlsx" \
  http://dev-server:8088/copias-seguridad/restaurar-backup?tipo=incremental
```

### Caso 3: Recuperación ante Desastres
```bash
# Restaurar completamente desde un backup conocido
curl -H "Authorization: Bearer $TOKEN" \
  -F "file=@backup-2026-04-18.xlsx" \
  http://localhost:8088/copias-seguridad/restaurar-backup?tipo=full-reescritura
```

## Notas Importantes

⚠️ **Seguridad:**
- Todos los endpoints requieren autenticación con rol `admin`
- Los archivos de backup contienen información sensible (incluye contraseñas hasheadas)
- Almacenar los backups en ubicaciones seguras

⚠️ **Rendimiento:**
- La generación de backups grandes puede tomar varios minutos
- La restauración con `full-reescritura` es más lenta que `incremental`
- Se recomienda ejecutar en horarios de bajo tráfico

✅ **Recomendaciones:**
- Realizar backups periódicos (diarios o semanales)
- Probar la restauración en ambiente de pruebas antes de usar en producción
- Verificar la integridad del archivo Excel antes de restaurar

---

# Monitor de Logs e InfluxDB

Proxy de consulta hacia InfluxDB para el dashboard de monitoreo de logs de backend y errores de frontend.

## Endpoints de Logs

### 1. Logs de Backend

Obtiene registros del measurement `backend_log` almacenados en InfluxDB.

**Endpoint:** `GET /api/v1/logs/backend`

**Headers:**
```
Authorization: Bearer {token}
```

**Parámetros (Query Params):**
- `from` (opcional): Fecha inicio (ej: `2026-04-01` o `2026-04-01T08:00:00Z`)
- `to` (opcional): Fecha fin (ej: `2026-04-25` o `2026-04-25T23:59:59Z`)
- `limit` (opcional): Máximo de filas a retornar (1-500)

**Ejemplo con cURL:**
```bash
curl --location 'http://localhost:8088/api/v1/logs/backend?limit=100' \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...'
```

### 2. Errores de Frontend

Obtiene registros del measurement `frontend_error` almacenados en InfluxDB.

**Endpoint:** `GET /api/v1/logs/frontend`

**Headers:**
```
Authorization: Bearer {token}
```

**Parámetros (Query Params):**
- `from` (opcional): Fecha inicio
- `to` (opcional): Fecha fin
- `limit` (opcional): Máximo de filas a retornar (1-500)

**Ejemplo con cURL:**
```bash
curl --location 'http://localhost:8088/api/v1/logs/frontend?from=2026-04-20&limit=50' \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...'
```

## Restricciones
- Solo usuarios con rol `admin` pueden acceder a estos endpoints.
- Los datos se retornan en formato JSON plano directamente desde la consulta a InfluxDB.
