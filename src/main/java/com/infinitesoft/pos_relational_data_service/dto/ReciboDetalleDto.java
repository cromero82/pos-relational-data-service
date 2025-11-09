package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboDetalleDto {
    private Long id;
    private Long reciboId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal subtotal;

    // Enriched product information
    private Product producto;
}
