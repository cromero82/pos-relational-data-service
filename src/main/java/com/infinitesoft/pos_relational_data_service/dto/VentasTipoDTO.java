package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentasTipoDTO {
    private Long id;
    private Long metodoPagoId;
    private BigDecimal total;
    private BigDecimal totalSistema;
    private Long corteVentaId;
}
