package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboDetalleResponse {
    private Long id;
    private Long reciboId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal subtotal;
    private LocalDateTime fechaCreacion;
    private UUID usuarioCreacion;
    private String nombreUsuarioAtendio;

    // Enriched product information (optional, but keep consistent with Dto if needed)
    private Product producto;
}
