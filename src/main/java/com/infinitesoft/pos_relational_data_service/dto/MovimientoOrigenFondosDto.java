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
    /** Id egreso / corte_venta / etc. según origenTipo. */
    private Long idReferencia;
    /** @deprecated usar {@link #idReferencia}; se mantiene por compatibilidad JSON. */
    private Long origenId;
    private String grupoTrasladoId;
    /** CUENTA_PERSONAL | ANTICIPO_SALARIO | VALE_EMPLEADO | GASTO_NEGOCIO | OTRO_LEGALIZADO */
    private String clasificacionOperativa;
    private Integer periodoCierreId;
}
