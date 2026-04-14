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
