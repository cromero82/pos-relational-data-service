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
    /**
     * Sesión de caja activa (panel de confirmación QR).
     * Si no se envía, se usa la sesión activa del usuario o la del recibo CxC.
     */
    private Long sesionId;
    /**
     * Cliente que entrega el abono. Opcional; si no viene, se usa el deudor de la CxC.
     */
    private Long clientePagadorId;
    /** Snapshot opcional del nombre (si no viene se resuelve desde client). */
    private String clientePagadorNombre;
}
