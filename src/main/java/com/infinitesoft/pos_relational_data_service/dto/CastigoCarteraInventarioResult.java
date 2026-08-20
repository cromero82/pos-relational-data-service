package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CastigoCarteraInventarioResult {
    private Long movimientoInventarioId;
    private BigDecimal valorPerdidaCosto;
}
