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
