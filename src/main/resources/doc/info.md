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
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "eventoId": 1,
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
