package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AbonoCxcDto {
    private Long id;
    private Long cuentaPorCobrarId;
    private LocalDateTime fechaAbono;
    private BigDecimal monto;
    private Long metodoPagoId;
    private String metodoPagoDescripcion;
    private Integer origenFondosId;
    private Long movimientoOrigenFondosId;
    private String observacion;
    /** True si el abono fue QR/Bancolombia y quedó pendiente de confirmación email. */
    private Boolean requiereConfirmacionElectronica;
}
