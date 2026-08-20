package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegistrarAbonoCxcRequest {
    /** Monto del abono (> 0 y ≤ saldo pendiente). */
    private BigDecimal monto;
    /** Medio de pago (resuelve OF raíz si no se envía origenFondosId). */
    private Long metodoPagoId;
    /** Opcional: override de origen de fondos. */
    private Integer origenFondosId;
    private String observacion;
}
