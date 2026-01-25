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
