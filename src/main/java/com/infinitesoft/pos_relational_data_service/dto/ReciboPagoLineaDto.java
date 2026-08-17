package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboPagoLineaDto {
    private Long metodoPagoId;
    private BigDecimal monto;
}
