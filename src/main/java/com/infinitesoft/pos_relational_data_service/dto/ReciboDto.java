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

    /**
     * Lista de IDs de métodos de pago asociados a este recibo (relación 1-N).
     * Reemplaza el campo único metodoPagoId.
     * El primer elemento de la lista se persiste también en la columna
     * metodo_pago_id de la tabla recibo por compatibilidad con datos históricos.
     */
    private List<Long> metodoPagoIds;

    private Long sesionId;

    private BigDecimal total;

    private BigDecimal montoRecibido;

    private Long reciboIdPadre;
}
