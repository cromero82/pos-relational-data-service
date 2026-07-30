package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoOrigenFondosDto {
    private Long id;
    private LocalDate fecha;
    private String usuarioId;
    private Integer origenFondosId;
    private String origenFondosNombre;
    private Integer origenDestinoId;
    private String origenDestinoNombre;
    private String tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal impacto;
    private BigDecimal saldoAntes;
    private BigDecimal saldoDespues;
    private Long metodoPagoId;
    private String terceroNombre;
    private Integer motivoMovimientoId;
    private String motivoMovimientoNombre;
    private String observacion;
    private BigDecimal valorSistema;
    private BigDecimal valorReal;
    private String origenTipo;
    private Long origenId;
    private String grupoTrasladoId;
}
