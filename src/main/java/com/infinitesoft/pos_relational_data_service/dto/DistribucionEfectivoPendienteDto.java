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
public class DistribucionEfectivoPendienteDto {
    private boolean pendiente;
    private Long corteVentaId;
    private Integer cajaEfectivoId;
    private String cajaEfectivoNombre;
    private BigDecimal saldoCajaEfectivo;
    private Integer cajaMenorId;
    private String cajaMenorNombre;
    private Integer cajaGeneralId;
    private String cajaGeneralNombre;
}
