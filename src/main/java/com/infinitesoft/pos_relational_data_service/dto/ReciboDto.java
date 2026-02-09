package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Client;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboDto {
    private Long id;

    private Long clienteId;
    private Client cliente; // full client info

    private LocalDateTime fechaCreacion;

    private Long estadoId;
    private String estado; // human readable label from enum

    private Long metodoPagoId;

    private Long sesionId;

    private BigDecimal total;

    private BigDecimal montoRecibido;
}
