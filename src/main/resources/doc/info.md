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

## Modificar configuración por Key

```bash
curl --location --request PUT 'http://localhost:8080/configuracion-app/key/total-productos' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "value": "150"
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

Búsqueda inteligente que incluye productos activos e inactivos (por defecto), maneja unión de palabras y términos desordenados.

**Parámetros:**
- `query`: Término de búsqueda (Nombre o código de barras).
- `page`: Número de página (opcional, defecto 0).
- `size`: Tamaño de página (opcional, defecto 10).
- `unicamenteActivos`: Si es `true`, filtra solo productos activos. Si es `false` (defecto), incluye todos.

```bash
curl --location 'http://localhost:8080/products/search?query=CERVEZA&page=0&size=10&unicamenteActivos=true' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Búsqueda de productos por filtros avanzados (JSON)

Permite realizar búsquedas dinámicas enviando un arreglo de filtros en el cuerpo de la petición.

**Estructura del filtro:**
- `campo`: Nombre del campo en la base de datos (ej: `nombre`, `precio`, `fechaCreacion`, `fechaUltimaActualizacionPrecio`, `totalVentas`).
- `condicion`: Operador de comparación (`=`, `>`, `>=`, `<`, `<=`, `like`).
- `valor`: El valor a comparar. Para fechas usar formato `dd/MM/yyyy`. Para nulos usar `"null"`.
- `campoOrdenamiento`: (Opcional, defecto: `nombre`) Campo por el cual ordenar los resultados.
- `orden`: (Opcional, defecto: `asc`) Dirección del ordenamiento (`asc` o `desc`).

**Ejemplo de filtros con ordenamiento:**

```json
{
  "filtros": [
    {
      "campo": "precio",
      "condicion": ">=",
      "valor": "10.5"
    }
  ],
  "page": 0,
  "size": 10,
  "campoOrdenamiento": "precio",
  "orden": "desc"
}
```

**Ejemplo de filtros:**

```json
{
  "filtros": [
    {
      "campo": "precio",
      "condicion": ">=",
      "valor": "10.5"
    },
    {
      "campo": "fechaUltimaActualizacionPrecio",
      "condicion": ">",
      "valor": "31/12/2025"
    }
  ],
  "page": 0,
  "size": 10
}
```

**Ejemplo de búsqueda por valor nulo:**

```json
{
  "filtros": [
    {
      "campo": "fechaUltimaActualizacionPrecio",
      "condicion": "=",
      "valor": "null"
    }
  ]
}
```

**Comando CURL:**

```bash
curl --location 'http://localhost:8080/products/busquedaPorFiltros' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
  "filtros": [
    {"campo": "precio", "condicion": ">=", "valor": "0"},
    {"campo": "fechaUltimaActualizacionPrecio", "condicion": ">", "valor": "31/12/2025"}
  ],
  "page": 0,
  "size": 10
}'
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

## Corrección Estructural: Mover `tipo_egreso_id` de `egreso` a `proveedor`

Para ajustar el diseño de la base de datos según el cambio solicitado, ejecuta las siguientes instrucciones SQL.

**Paso 1: Añadir la columna a la tabla `proveedor`**
```sql
ALTER TABLE proveedor ADD COLUMN tipo_egreso_id INT;
```

**Paso 2: Añadir la restricción de clave foránea**
```sql
ALTER TABLE proveedor 
ADD CONSTRAINT fk_proveedor_tipo_egreso 
FOREIGN KEY (tipo_egreso_id) 
REFERENCES tipo_egreso(id);
```

**Paso 3: (Opcional) Migrar datos existentes**
Este paso es crucial si ya tienes datos. Asumiremos que cada proveedor tomará el `tipo_egreso_id` del primer registro de `egrego` que tenga asociado. Si un proveedor no tiene egresos, su `tipo_egreso_id` quedará nulo.

```sql
UPDATE proveedor p
SET tipo_egreso_id = (
    SELECT e.tipo_egreso_id
    FROM egreso e
    WHERE e.proveedor_id = p.id
    ORDER BY e.fecha ASC, e.id ASC
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1
    FROM egreso e
    WHERE e.proveedor_id = p.id
);
```

**Paso 4: Eliminar la columna y la restricción de la tabla `egreso`**
Una vez que los datos estén migrados y la aplicación actualizada, puedes eliminar la columna de la tabla `egreso`.

```sql
-- Primero, eliminar la restricción de clave foránea
ALTER TABLE egreso DROP CONSTRAINT fk_tipo_egreso;

-- Luego, eliminar la columna
ALTER TABLE egreso DROP COLUMN tipo_egreso_id;
```

# Scripts de Creación de Tablas Principales

## Tabla Egreso
```sql
CREATE TABLE public.egreso (
	id serial4 NOT NULL,
	fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	fecha date NOT NULL,
	valor numeric(15, 2) NOT NULL,
	descripcion text NULL,
	proveedor_id int4 NULL,
	CONSTRAINT egreso_pkey PRIMARY KEY (id)
);

-- public.egreso foreign keys

ALTER TABLE public.egreso ADD CONSTRAINT fk_proveedor FOREIGN KEY (proveedor_id) REFERENCES public.proveedor(id);
```

## Tabla Tipo Resultado Financiero
```sql
CREATE TABLE tipo_resultado_fin (
    id SERIAL PRIMARY KEY,
    sigla VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    color VARCHAR(7) NOT NULL DEFAULT '#000000' CHECK (color ~ '^#[0-9A-Fa-f]{6}$')
);

INSERT INTO tipo_resultado_fin (sigla, descripcion, color) VALUES
('no_ventas', 'No se registraron ventas', '#808080'),
('no_egresos', 'No se registraron egresos', '#808080'),
('util_menos_5p', 'Utilidad menor del 5%', '#E6FFFA'),
('util_5p_10p', 'Utilidad entre el 5% y 10%', '#B2F5EA'),
('util_10p_20p', 'Utilidad entre el 10% y 20%', '#81E6D9'),
('util_20p_30p', 'Utilidad entre el 20% y 30%', '#4FD1C5'),
('util_30p_40p', 'Utilidad entre el 30% y 40%', '#38B2AC'),
('util_40p_50p', 'Utilidad entre el 40% y 50%', '#319795'),
('util_50p_60p', 'Utilidad entre el 50% y 60%', '#2C7A7B'),
('util_60p_70p', 'Utilidad entre el 60% y 70%', '#285E61'),
('util_mayor_70p', 'Utilidad mayor a 70%', '#234E52'),
('p_menos_5p', 'Pérdida menor a 5%', '#FFF5F5'),
('p_5p_10p', 'Pérdida entre 5% y 10%', '#FED7D7'),
('p_10p_20p', 'Pérdida entre 10% y 20%', '#FEB2B2'),
('p_20p_30p', 'Pérdida entre 20% y 30%', '#FCA5A5'),
('p_mayor_30p', 'Pérdida mayor a 30%', '#FC8181');
```

## Tabla Estadística Financiera
```sql
CREATE TABLE estadistica_fin (
    id SERIAL PRIMARY KEY,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_egresos NUMERIC(15,2),
    total_ventas NUMERIC(15,2),
    utilidad NUMERIC(15,2),
    porcentaje_utilidad NUMERIC(6,2) CHECK (porcentaje_utilidad >= -100 AND porcentaje_utilidad <= 100),
    formato_tiempo VARCHAR(10) NOT NULL CHECK (formato_tiempo IN ('DIA','MES','ANIO')),
    valor_tiempo VARCHAR(20) NOT null,
    tipo_resultado_fin_id INT,
    CONSTRAINT fk_tipo_resultado_fin FOREIGN KEY (tipo_resultado_fin_id) REFERENCES tipo_resultado_fin (id)
);
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

# UsuarioPerfil Endpoints

## Obtener todos los perfiles de usuario

```bash
curl --location 'http://localhost:8080/usuario-perfil' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener perfil de usuario por ID

```bash
curl --location 'http://localhost:8080/usuario-perfil/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Crear perfil de usuario

```bash
curl --location 'http://localhost:8080/usuario-perfil' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "personalizacion": {
        "tema": "oscuro",
        "notificaciones": true
    }
}'
```

## Actualizar perfil de usuario

```bash
curl --location --request PUT 'http://localhost:8080/usuario-perfil/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "personalizacion": {
        "tema": "claro",
        "notificaciones": false
    }
}'
```

## Eliminar perfil de usuario

```bash
curl --location --request DELETE 'http://localhost:8080/usuario-perfil/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener perfiles de usuario por usuario_id

```bash
curl --location 'http://localhost:8080/usuario-perfil/usuario/{usuarioId}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# Proveedor Endpoints

## Crear proveedor

```bash
curl --location 'http://localhost:8080/proveedores' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "documento": "123456789",
    "nombre": "PROVEEDOR EJEMPLO S.A.S",
    "telefono": "3001234567",
    "correo": "contacto@proveedor.com",
    "tipoEgresoId": 1
}'
```

*Nota: La creación de un proveedor genera automáticamente un registro en la bitácora de usuario con la sigla `REG_PROVEEDOR`.*

## Obtener todos los proveedores

```bash
curl --location 'http://localhost:8080/proveedores' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener proveedor por ID

```bash
curl --location 'http://localhost:8080/proveedores/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Obtener proveedor por Documento

```bash
curl --location 'http://localhost:8080/proveedores/documento/{documento}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Actualizar proveedor

```bash
curl --location --request PUT 'http://localhost:8080/proveedores/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE' \
--data '{
    "documento": "123456789",
    "nombre": "PROVEEDOR EJEMPLO MODIFICADO",
    "telefono": "3007654321",
    "correo": "nuevo_contacto@proveedor.com",
    "tipoEgresoId": 2
}'
```

*Nota: La actualización de un proveedor genera automáticamente un registro en la bitácora de usuario con la sigla `MOD_PROVEEDOR`.*

## Eliminar proveedor

```bash
curl --location --request DELETE 'http://localhost:8080/proveedores/{id}' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

# TipoEgreso Endpoints

## Obtener todos los tipos de egreso

```bash
curl --location 'http://localhost:{{port}}/tipo_egresos' \
--header 'Authorization: Bearer {{token}}'
```

## Obtener tipo de egreso por ID

```bash
curl --location 'http://localhost:{{port}}/tipo_egresos/{id}' \
--header 'Authorization: Bearer {{token}}'
```

## Crear tipo de egreso

```bash
curl --location 'http://localhost:{{port}}/tipo_egresos' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{token}}' \
--data '{
    "nombre": "Gastos Operativos",
    "descripcion": "Gastos relacionados con la operación diaria"
}'
```

## Actualizar tipo de egreso

```bash
curl --location --request PUT 'http://localhost:{{port}}/tipo_egresos/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{token}}' \
--data '{
    "nombre": "Gastos Administrativos",
    "descripcion": "Gastos de administración"
}'
```

## Eliminar tipo de egreso

```bash
curl --location --request DELETE 'http://localhost:{{port}}/tipo_egresos/{id}' \
--header 'Authorization: Bearer {{token}}'
```

# Egreso Endpoints

## Obtener todos los egresos

```bash
curl --location 'http://localhost:{{port}}/egreso' \
--header 'Authorization: Bearer {{token}}'
```

## Obtener egreso por ID

```bash
curl --location 'http://localhost:{{port}}/egreso/{id}' \
--header 'Authorization: Bearer {{token}}'
```

## Crear egreso

```bash
curl --location 'http://localhost:{{port}}/egreso' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{token}}' \
--data '{
    "fecha": "2025-10-20",
    "valor": 1500.00,
    "descripcion": "Pago de servicios públicos",
    "proveedor": {
        "id": 1
    }
}'
```

## Actualizar egreso

```bash
curl --location --request PUT 'http://localhost:{{port}}/egreso/{id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{token}}' \
--data '{
    "fecha": "2025-10-21",
    "valor": 1600.00,
    "descripcion": "Pago corregido",
    "proveedor": {
        "id": 1
    }
}'
```

## Eliminar egreso

```bash
curl --location --request DELETE 'http://localhost:{{port}}/egreso/{id}' \
--header 'Authorization: Bearer {{token}}'
```

## Buscar egresos por rango de fechas

```bash
curl --location 'http://localhost:{{port}}/egreso/rango_fechas?fechaInicio=2025-10-01&fechaFin=2025-10-31' \
--header 'Authorization: Bearer {{token}}'
```

## Buscar egresos por proveedor

```bash
curl --location 'http://localhost:{{port}}/egreso/proveedor/{proveedorId}' \
--header 'Authorization: Bearer {{token}}'
```

## Buscar egresos por tipo de egreso

```bash
curl --location 'http://localhost:{{port}}/egreso/tipo_egreso/{tipoEgresoId}' \
--header 'Authorization: Bearer {{token}}'
```
## Búsqueda paginada de egresos
Busca y pagina los egresos, ordenados por fecha descendente.
**Parámetros:**
- `descripcion`: (Opcional) Texto para buscar en la descripción del egreso.
- `tipoEgresoId`: (Opcional) ID del tipo de egreso para filtrar (a través del proveedor).
- `proveedorId`: (Opcional) ID del proveedor para filtrar.
- `page`: (Opcional) Número de página (defecto: 0).
- `size`: (Opcional) Tamaño de la página (defecto: 10).

### Búsqueda por descripción y tipo de egreso
```bash
curl --location 'http://localhost:{{port}}/egreso/search?descripcion=pago&tipoEgresoId=1' \
--header 'Authorization: Bearer {{token}}'
```

### Búsqueda con todos los filtros
```bash
curl --location 'http://localhost:{{port}}/egreso/search?descripcion=compra&tipoEgresoId=2&proveedorId=5&page=0&size=5' \
--header 'Authorization: Bearer {{token}}'
```

## Búsqueda global por descripción
Busca un texto en la descripción del egreso, el nombre del proveedor y el nombre del tipo de egreso.
```bash
curl --location 'http://localhost:{{port}}/egreso/searchDescripciones?descripcion=pago&page=0&size=10' \
--header 'Authorization: Bearer {{token}}'
```

# Estadistica Financiera Endpoints

## Crear estadística financiera (Asíncrono)
Inicia el proceso de cálculo de estadísticas para un periodo determinado. Detecta automáticamente el formato (DIA, MES, ANIO) basándose en `valorTiempo`.
Si el registro ya existe, devuelve error `409 Conflict`.
```bash
curl --location 'http://localhost:{{port}}/estadistica-financiera' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{token}}' \
--data '{
    "valorTiempo": "2026-01-20"
}'
```

## Actualizar estadística financiera (Síncrono)
Recalcula y sobreescribe una estadística financiera existente. Devuelve el objeto actualizado o error `404 Not Found` si no existe.
```bash
curl --location --request PUT 'http://localhost:{{port}}/estadistica-financiera' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{token}}' \
--data '{
    "valorTiempo": "2026-01-20"
}'
```

## Consultar estadísticas diarias (Paginado)
Retorna estadísticas financieras en formato diario. El campo `valorTiempo` se convierte a un objeto `Date` llamado `dia`.
```bash
curl --location 'http://localhost:{{port}}/estadistica-financiera/diaria?page=0&size=10' \
--header 'Authorization: Bearer {{token}}'
```

## Consultar estadísticas diarias (Paginado con filtros de fecha)
Retorna estadísticas financieras en formato diario, filtradas por un rango de fechas.
```bash
curl --location 'http://localhost:{{port}}/estadistica-financiera/diaria?fechaInicio=2024-01-01&fechaFin=2024-01-31&page=0&size=10' \
--header 'Authorization: Bearer {{token}}'
```

## Consultar estadísticas mensuales
Retorna estadísticas financieras en formato mensual. El campo `valorTiempo` se convierte a un objeto `Date` llamado `mes`.
```bash
curl --location 'http://localhost:{{port}}/estadistica-financiera/mensual' \
--header 'Authorization: Bearer {{token}}'
```

## Consultar estadísticas anuales
Retorna estadísticas financieras en formato anual. El campo `valorTiempo` se convierte a un objeto `Date` llamado `anio`.
```bash
curl --location 'http://localhost:{{port}}/estadistica-financiera/anual' \
--header 'Authorization: Bearer {{token}}'
```
