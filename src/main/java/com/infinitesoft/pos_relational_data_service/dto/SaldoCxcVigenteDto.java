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
public class SaldoCxcVigenteDto {
    /** Σ saldo pendiente de CxC ABIERTA/PARCIAL. */
    private BigDecimal saldo;
    /** Σ (totalTicket|montoOriginal − saldo) de vigentes: abono inicial + cobranzas. */
    private BigDecimal cobrada;
}
