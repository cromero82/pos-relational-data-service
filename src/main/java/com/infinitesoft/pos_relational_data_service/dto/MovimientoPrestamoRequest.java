package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoPrestamoRequest {
    private Integer origenFondosId;
    private BigDecimal valor;
    private LocalDate fecha;
    private String terceroNombre;
    private String observacion;
}
