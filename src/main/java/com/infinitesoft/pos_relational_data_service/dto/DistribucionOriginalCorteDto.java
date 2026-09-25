package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Monto que el corte originalmente distribuyó a Caja Menor / Caja General (SPLIT: sirve para
 * prellenar y validar en el FE que el prorrateo entre particiones suma exactamente esto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistribucionOriginalCorteDto {
    private Long corteVentaId;
    private Integer cajaMenorId;
    private BigDecimal montoCajaMenor;
    private Integer cajaGeneralId;
    private BigDecimal montoCajaGeneral;
}
