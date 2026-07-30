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
public class DistribucionEfectivoRequest {
    /** Monto que permanece en Caja: Efectivo (= Base del próximo turno). */
    private BigDecimal base;
    /** Traslado a Caja Menor (puede ser 0). */
    private BigDecimal montoCajaMenor;
    /** Traslado a Caja General (puede ser 0). */
    private BigDecimal montoCajaGeneral;
    private String observacion;
}
