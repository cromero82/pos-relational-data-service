package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CuentaPorCobrarDto {
    private Long id;
    private Long historialReciboId;
    private Long documentoVentaId;
    private Long reciboId;
    private Long ticketId;
    private Long clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteCorreo;
    private LocalDateTime fechaOrigen;
    private BigDecimal montoOriginal;
    private BigDecimal saldoPendiente;
    private String estado;
    private String observacion;
}
