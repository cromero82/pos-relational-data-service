# Health Check Endpoint

To check the health of the application, you can use the following endpoint:

```
GET /actuator/health
```

Here is a `curl` command you can use in Postman or your terminal:

```bash
curl --location --request GET 'http://localhost:8080/actuator/health'
```

# ConfiguracionApp Endpoints

## Obtener todas las configuraciones

```bash
curl --location --request GET 'http://localhost:8080/configuracion-app/obtenerTodos' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Modificar configuración

```bash
curl --location --request PUT 'http://localhost:8080/configuracion-app/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "clave": "string",
    "valor": "string"
}'
```

# Evento Endpoints

## Obtener todos los eventos

```bash
curl --location 'http://localhost:8080/api/evento' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener evento por ID

```bash
curl --location 'http://localhost:8080/api/evento/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Crear evento

```bash
curl --location 'http://localhost:8080/api/evento' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "nombre": "Nuevo Evento",
    "sigla": "NUEVO_EVT"
}'
```

## Actualizar evento

```bash
curl --location --request PUT 'http://localhost:8080/api/evento/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "nombre": "Evento Actualizado",
    "sigla": "ACT_EVT"
}'
```

## Eliminar evento

```bash
curl --location --request DELETE 'http://localhost:8080/api/evento/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# BitacoraUsuario Endpoints

## Obtener todas las bitácoras

```bash
curl --location 'http://localhost:8080/api/bitacora-usuario' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener bitácora por ID

```bash
curl --location 'http://localhost:8080/api/bitacora-usuario/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Búsqueda paginada y con filtros

### Búsqueda paginada simple (página 0, 10 resultados por página)
```bash
curl --location 'http://localhost:8080/api/bitacora-usuario/search?page=0&size=10' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

### Búsqueda por `userId`
```bash
curl --location 'http://localhost:8080/api/bitacora-usuario/search?userId=123e4567-e89b-12d3-a456-426614174000' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

### Búsqueda por `fecha`
```bash
curl --location 'http://localhost:8080/api/bitacora-usuario/search?fecha=2025-12-20' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

### Búsqueda combinada y ordenada por fecha descendente
```bash
curl --location 'http://localhost:8080/api/bitacora-usuario/search?userId=123e4567-e89b-12d3-a456-426614174000&fecha=2025-12-20&sort=fechaCreacion,desc' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# HistorialRecibo Endpoints

## Búsqueda paginada y con filtros

### Búsqueda por `sesionId`
Filtra exclusivamente por `sesionId`. Si se proporciona, se ignoran los parámetros de paginación.
```bash
curl --location 'http://localhost:8080/historial-recibos/search?sesionId=123' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

### Búsqueda por `fecha` y `estadoId` (Paginada)
```bash
curl --location 'http://localhost:8080/historial-recibos/search?fecha=2025-12-20&estadoId=1&page=0&size=10' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Crear historial de recibo
```bash
curl --location 'http://localhost:8080/historial-recibos' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "clienteId": 1,
    "estadoId": 2,
    "metodoPagoId": 1,
    "sesionId": 10,
    "total": 150.50,
    "montoRecibido": 200.00
}'
```

## Crear historial de recibo rápido (Estado PAGADO)
```bash
curl --location 'http://localhost:8080/historial-recibos/addquickRecibo' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "clienteId": 1,
    "metodoPagoId": 1,
    "sesionId": 10,
    "total": 150.50,
    "montoRecibido": 150.50
}'
```

## Actualizar historial de recibo
```bash
curl --location --request PUT 'http://localhost:8080/historial-recibos/{id}?sesionId=10' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "clienteId": 1,
    "estadoId": 2,
    "metodoPagoId": 1,
    "total": 150.50,
    "montoRecibido": 200.00
}'
```

# Recibo Endpoints

## Crear recibo
```bash
curl --location 'http://localhost:8080/recibos' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "clienteId": 1,
    "estadoId": 1,
    "metodoPagoId": 1,
    "sesionId": 10,
    "total": 100.00,
    "montoRecibido": 100.00
}'
```

## Obtener recibo por ID
```bash
curl --location 'http://localhost:8080/recibos/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Actualizar recibo
```bash
curl --location --request PUT 'http://localhost:8080/recibos/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "clienteId": 1,
    "estadoId": 2,
    "metodoPagoId": 1,
    "total": 100.00,
    "montoRecibido": 100.00
}'
```

# EdicionRecibo Endpoints

## Crear edición de recibo
```bash
curl --location 'http://localhost:8080/edicion-recibos' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "reciboId": 5,
    "historialReciboId": 3,
    "clienteId": 1,
    "estadoId": 1,
    "metodoPagoId": 1,
    "sesionId": 10,
    "total": 120.00,
    "montoRecibido": 120.00
}'
```

## Actualizar edición de recibo
```bash
curl --location --request PUT 'http://localhost:8080/edicion-recibos/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "reciboId": 5,
    "clienteId": 1,
    "estadoId": 1,
    "metodoPagoId": 1,
    "sesionId": 10,
    "total": 130.00,
    "montoRecibido": 150.00
}'
```

## Crear bitácora

```bash
curl --location 'http://localhost:8080/api/bitacora-usuario' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "evento": "SIGLA_EVENTO",
    "referenciaId": 123,
    "valorAntes": "{\"campo\": \"valor_anterior\"}",
    "valorDespues": "{\"campo\": \"valor_nuevo\"}"
}'
```

O usando `eventoId` directamente:

```bash
curl --location 'http://localhost:8080/api/bitacora-usuario' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "eventoId": 1,
    "referenciaId": 123,
    "valorAntes": "{\"campo\": \"valor_anterior\"}",
    "valorDespues": "{\"campo\": \"valor_nuevo\"}"
}'
```

## Actualizar bitácora

```bash
curl --location --request PUT 'http://localhost:8080/api/bitacora-usuario/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "eventoId": 1,
    "valorAntes": "{\"campo\": \"valor_anterior_modificado\"}",
    "valorDespues": "{\"campo\": \"valor_nuevo_modificado\"}"
}'
```

## Eliminar bitácora

```bash
curl --location --request DELETE 'http://localhost:8080/api/bitacora-usuario/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# Sesion Endpoints

## Obtener todas las sesiones activas del usuario

```bash
curl --location 'http://localhost:8080/sesiones' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener todas las sesiones (activas e inactivas) del usuario

```bash
curl --location 'http://localhost:8080/sesiones/usuario/todas' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener información del usuario asociado a una sesión

```bash
curl --location 'http://localhost:8080/sesiones/{id}/usuario' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# Product Endpoints

## Crear producto

```bash
curl --location 'http://localhost:8080/products' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "barcode": "123456789",
    "nombre": "Producto Ejemplo",
    "precio": 100.0,
    "precioCompra": 80.0
}'
```

*Nota: La creación de un producto genera automáticamente un registro en la bitácora de usuario con la sigla `REG_PROD`.*

## Actualizar producto

```bash
curl --location --request PUT 'http://localhost:8080/products/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "barcode": "123456789-MOD",
    "nombre": "Producto Ejemplo Modificado",
    "precio": 110.0,
    "precioCompra": 85.0
}'
```

*Nota: La actualización de un producto genera automáticamente un registro en la bitácora de usuario con la sigla `MOD_PROD`, guardando el estado anterior y el nuevo.*

## Deshabilitar producto

```bash
curl --location --request PATCH 'http://localhost:8080/products/{id}/deactivate' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

*Nota: Esta operación cambia el estado `activate` a 0 y genera automáticamente un registro en la bitácora de usuario con la sigla `DESHAB_PROD`.*

## Habilitar producto

```bash
curl --location --request PATCH 'http://localhost:8080/products/{id}/activate' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

*Nota: Esta operación cambia el estado `activate` a 1 y genera automáticamente un registro en la bitácora de usuario con la sigla `HAB_PROD`.*

## Buscar producto por código de barras

```bash
curl --location 'http://localhost:8080/products/search-by-barcode?barcode=123456789' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Búsqueda paginada de productos (por nombre o código de barras)

Búsqueda inteligente que incluye productos activos e inactivos, maneja unión de palabras y términos desordenados.

```bash
curl --location 'http://localhost:8080/products/search?query=CERVEZA&page=0&size=10' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# CargueProducto Endpoints

## Obtener todos los registros de cargue

```bash
curl --location 'http://localhost:8080/api/cargue-productos' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Crear registro de cargue (Importar Archivo)

Este endpoint procesa un archivo (Excel o CSV) y crea un registro de cargue con sus conflictos asociados.

```bash
curl --location 'http://localhost:8080/api/cargue-productos' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--form 'file=@"/path/to/your/file.xlsx"' \
--form 'nombre="Cargue Diciembre 2025"'
```

# CargueProductoConflicto Endpoints

## Obtener conflictos por ID de cargue (Obligatorio) y opcionalmente por estado resuelto

```bash
curl --location 'http://localhost:8080/api/cargue-producto-conflictos?cargueProductoId=1&unicamenteNoResueltos=true' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Crear conflicto de cargue

```bash
curl --location 'http://localhost:8080/api/cargue-producto-conflictos' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "cargueProducto": {
        "id": 1
    },
    "tipoConflictoId": 1,
    "nombreProducto": "Producto Duplicado",
    "datosConflicto": "{\"codigoBarras\": \"123456789\", \"motivo\": \"Nombre duplicado\"}",
    "resuelto": false
}'
```

## Resolver conflicto

Marca un conflicto como resuelto y actualiza el contador en el cargue asociado.

```bash
curl --location --request PUT 'http://localhost:8080/api/cargue-producto-conflictos/{id}/resolver' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# VentasTipo Endpoints

## Obtener todas las ventas por tipo

```bash
curl --location --request GET 'http://localhost:8080/ventas-tipo' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener ventas por ID

```bash
curl --location --request GET 'http://localhost:8080/ventas-tipo/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Crear venta por tipo

```bash
curl --location --request POST 'http://localhost:8080/ventas-tipo' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "metodoPagoId": 1,
    "total": 1500.50,
    "totalSistema": 1450.00,
    "corteVentaId": 10
}'
```

## Actualizar venta por tipo

```bash
curl --location --request PUT 'http://localhost:8080/ventas-tipo/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "metodoPagoId": 1,
    "total": 1600.00,
    "totalSistema": 1550.00,
    "corteVentaId": 11
}'
```

## Eliminar venta por tipo

```bash
curl --location --request DELETE 'http://localhost:8080/ventas-tipo/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# CorteVenta Endpoints

## Obtener todos los cortes de venta

```bash
curl --location --request GET 'http://localhost:8080/corte-venta' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener corte de venta por ID

```bash
curl --location --request GET 'http://localhost:8080/corte-venta/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Crear corte de venta

```bash
curl --location --request POST 'http://localhost:8080/corte-venta' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
    "fechaIni": "2026-01-27T08:00:00",
    "fechaFin": "2026-01-27T17:00:00",
    "ultimoHistorialReciboId": 101,
    "total": 5000.00,
    "totalSistema": 4950.00,
    "ultimoCorte": true,
    "actual": true,
    "ventasTipo": [
        {
            "metodoPagoId": 1,
            "total": 2500.00,
            "totalSistema": 2450.00
        },
        {
            "metodoPagoId": 2,
            "total": 2500.00,
            "totalSistema": 2500.00
        }
    ]
}'
```

*Nota: Si `ultimoCorte` y `actual` son `true`, `fechaIni`, `fechaFin` y `totalSistema` (tanto del corte como de los detalles) se calcularán automáticamente basados en el historial de recibos.*

## Actualizar corte de venta

```bash
curl --location --request PUT 'http://localhost:8080/corte-venta/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
    "fechaIni": "2026-01-27T08:00:00",
    "fechaFin": "2026-01-27T18:00:00",
    "ultimoHistorialReciboId": 101,
    "total": 5100.00,
    "totalSistema": 5050.00,
    "ventasTipo": [
        {
            "id": 1,
            "metodoPagoId": 1,
            "total": 2600.00,
            "totalSistema": 2550.00
        }
    ]
}'
```

## Eliminar corte de venta

```bash
curl --location --request DELETE 'http://localhost:8080/corte-venta/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Consultar rango de corte

```bash
curl --location --request GET 'http://localhost:8080/corte-venta/consultar-rango?fechaIni=2026-01-31T08:00:00&fechaFin=2026-01-31T18:00:00&ultimoCorte=true&actual=true' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Buscar cortes por rango de fechas

```bash
curl --location --request GET 'http://localhost:8080/corte-venta/search?fechaIni=2026-01-01T00:00:00&fechaFin=2026-01-31T23:59:59' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# Ticket Endpoints

## Crear ticket
El campo `orden` se calcula automáticamente si se proporciona `sessionId` (max + 1).

```bash
curl --location 'http://localhost:8080/tickets' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "sessionId": 61,
    "nombre": "Nuevo Ticket"
}'
```

## Obtener tickets por sesión (Enriquecido)
Obtiene todos los tickets de una sesión. Incluye información detallada del cliente y del usuario que atendió (si el cliente no es anónimo). El campo `perteneceUsuarioActual` indica si el ticket fue creado por el usuario logueado.

```bash
curl --location 'http://localhost:8088/tickets/session/{sessionId}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

*Nota: La información del cliente y de quien atendió se omite si el cliente es el usuario anónimo (ID configurado en `app.id-usuario-anonimo`).*

## Actualizar cliente de un ticket
Actualiza el cliente asociado a un ticket específico navegando hasta el recibo correspondiente.

```bash
curl --location --request PUT 'http://localhost:8088/tickets/actualizaCliente' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "ticketId": 95,
    "clienteId": 2
}'
```

## Actualización masiva de tickets (Reordenamiento)

```bash
curl --location --request PUT 'http://localhost:8080/tickets' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '[
    {
        "id": 80,
        "sessionId": 61,
        "nombre": "Ticket 1",
        "orden": 2
    },
    {
        "id": 94,
        "sessionId": 61,
        "nombre": "Ticket 2",
        "orden": 3
    },
    {
        "id": 95,
        "sessionId": 61,
        "nombre": "Ticket 3",
        "orden": 1
    }
]'
```

# Mantenimiento de Base de Datos

## Truncar milisegundos en fechas existentes (PostgreSQL)

Para asegurar la consistencia con el nuevo sistema que omite milisegundos, se recomienda ejecutar la siguiente instrucción SQL para truncar los valores existentes en las tablas principales:

```sql
-- Truncar milisegundos en historial_recibo
UPDATE historial_recibo SET fecha_creacion = date_trunc('second', fecha_creacion);

-- Truncar milisegundos en corte_venta
UPDATE corte_venta SET 
    fecha_creacion = date_trunc('second', fecha_creacion),
    fecha_ini = date_trunc('second', fecha_ini),
    fecha_fin = date_trunc('second', fecha_fin);

-- Truncar milisegundos en bitacora_usuario
UPDATE bitacora_usuario SET fecha_creacion = date_trunc('second', fecha_creacion);

-- Truncar milisegundos en producto
UPDATE producto SET 
    fecha_creacion = date_trunc('second', fecha_creacion),
    fecha_actualizacion_precio = date_trunc('second', fecha_actualizacion_precio);

-- Truncar milisegundos en cargue_productos
UPDATE cargue_productos SET fecha_creacion = date_trunc('second', fecha_creacion);

-- Truncar milisegundos en sesion
UPDATE sesion SET 
    fecha_inicio = date_trunc('second', fecha_inicio),
    fecha_fin = date_trunc('second', fecha_fin);
```

# Copias de Seguridad Endpoints

## Generar Backup Excel

Este endpoint genera un archivo Excel con todas las tablas principales de la base de datos (Productos, Historiales, Cortes de Venta, Bitácora, etc.) y la lista de usuarios.

```bash
curl --location 'http://localhost:8088/copias-seguridad/generar-backup' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

*Nota: Este es un proceso asíncrono que puede tardar varios segundos. El servidor tiene un timeout extendido de 30 segundos para esta operación. Solo los usuarios con el rol `admin` están autorizados para realizar esta acción.*

## Exportar Backup a Correo

Genera un backup en Excel y lo envía automáticamente al correo electrónico del usuario que realiza la petición (extraído del token).

```bash
curl --location 'http://localhost:8088/copias-seguridad/exportar-a-correo' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

*Nota: Al igual que la generación de backup, este proceso es asíncrono y requiere privilegios de `admin`.*
