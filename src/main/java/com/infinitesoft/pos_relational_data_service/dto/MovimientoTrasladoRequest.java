package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoTrasladoRequest {
    private Integer origenFondosId;
    private Integer origenDestinoId;
    private BigDecimal valor;
    private LocalDate fecha;
    private String observacion;
}
