package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbrirCuentaPorCobrarRequest {
    private Long ticketId;
    private Long reciboId;
    private Long clienteId;
    private String clienteNombre;
    private String telefono;
    private String correo;
    private String documento;
    /**
     * Total del ticket al abrir (suma de productos en UI).
     * Se usa para sincronizar {@code recibo.total} si en BD aún está en 0.
     */
    private BigDecimal totalTicket;
    /** Abono de contado al abrir (puede ser 0 = todo a crédito). */
    private BigDecimal abono;
    /**
     * Saldo que queda a crédito (= totalTicket − abono, o el monto declarado).
     * Es el {@code monto_original} / {@code saldo_pendiente} de la CxC.
     */
    private BigDecimal monto;
    private String observacion;
}
