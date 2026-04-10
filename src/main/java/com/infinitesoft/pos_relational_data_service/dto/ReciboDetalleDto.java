package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalleHistorico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private LocalDateTime fechaCreacion;
    private UUID usuarioCreacion;

    // Enriched product information
    private Product producto;

    private String nombreUsuarioAtendio;

    private List<ReciboDetalleHistorico> historicoAcciones;

    public ReciboDetalleDto(Long id, Long reciboId, Long productoId, Integer cantidad, BigDecimal subtotal, LocalDateTime fechaCreacion, UUID usuarioCreacion, Product producto) {
        this.id = id;
        this.reciboId = reciboId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.fechaCreacion = fechaCreacion;
        this.usuarioCreacion = usuarioCreacion;
        this.producto = producto;
    }
}
