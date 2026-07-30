package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoVentaDto {
    private Long id;
    private String consecutivo;
    private Integer anio;
    private Long historialReciboId;
    private LocalDateTime fechaHecho;
    private BigDecimal total;
    private Long metodoPagoId;
    private Long clienteId;
    private String estado;
}
