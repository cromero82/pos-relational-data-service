package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class EntradaInventarioDetalleRequest {

    @NotNull(message = "productoId es obligatorio")
    private Long productoId;

    @NotNull(message = "cantidad es obligatoria")
    @Min(value = 1, message = "cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "precioCompra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "precioCompra no puede ser negativo")
    private BigDecimal precioCompra;

    @DecimalMin(value = "0.0", inclusive = true, message = "precioVenta no puede ser negativo")
    private BigDecimal precioVenta;
}
