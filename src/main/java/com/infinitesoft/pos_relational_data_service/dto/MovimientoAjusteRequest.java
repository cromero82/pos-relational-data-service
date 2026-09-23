package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoAjusteRequest {
    private Integer origenFondosId;
    private BigDecimal valorSistema;
    private BigDecimal valorReal;
    private LocalDate fecha;
    private Integer motivoMovimientoId;
    private String observacion;
}
