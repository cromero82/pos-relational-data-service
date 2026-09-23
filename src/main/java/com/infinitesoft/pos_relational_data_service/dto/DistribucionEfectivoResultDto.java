package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistribucionEfectivoResultDto {
    private boolean ok;
    private Long corteVentaId;
    private BigDecimal baseSiguienteEfectivo;
    private List<MovimientoOrigenFondosDto> movimientos;
}
