package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteKpiReferenciaDto {
    /** Cartera del último corte vigente (snapshot). */
    private BigDecimal kpiCarteraAnterior;
    private LocalDateTime kpiCarteraAnteriorFecha;
    /** Ventas turno del corte de referencia (ayer / turno similar). */
    private BigDecimal kpiVentasTurnoReferencia;
    private LocalDateTime kpiVentasTurnoReferenciaFecha;
}
