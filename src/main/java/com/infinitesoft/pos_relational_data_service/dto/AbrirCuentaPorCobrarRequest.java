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
     * Medio del abono inicial. Obligatorio si {@code abono} &gt; 0
     * (queda en {@code abono_cxc} y alimenta historial multipago al liquidar).
     */
    private Long metodoPagoId;
    /** Opcional: override de origen de fondos del abono inicial. */
    private Integer origenFondosId;
    /**
     * Saldo que queda a crédito (= totalTicket − abono, o el monto declarado).
     * Es el {@code monto_original} / {@code saldo_pendiente} de la CxC.
     */
    private BigDecimal monto;
    private String observacion;
    /**
     * Sesión de caja activa (panel confirmación QR del abono inicial).
     * Preferible a la sesion_id histórica del recibo/ticket.
     */
    private Long sesionId;
    /**
     * HRE ya confirmado (faltante QR venta): al crear el abono inicial se retargeta
     * a {@code abono_cxc_id} en vez de crear un pendiente CREADA nuevo.
     */
    private Long historialElectronicoId;
}
