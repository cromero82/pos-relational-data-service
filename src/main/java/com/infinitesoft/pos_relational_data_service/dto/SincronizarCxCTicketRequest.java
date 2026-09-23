package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SincronizarCxCTicketRequest {
    /** Nuevo total del ticket (suma de productos). */
    private BigDecimal totalTicket;
}
