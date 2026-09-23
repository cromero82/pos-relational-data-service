package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Client;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    private Long reciboIdPadre;

    /**
     * Desglose de cobro (1–3 medios). Si viene vacío/null al pagar, se crea una línea
     * con {@code metodoPagoId} + {@code total}.
     */
    private List<ReciboPagoLineaDto> pagos;
}
