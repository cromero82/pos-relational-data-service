package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {
    private Long id;
    private Long sessionId;
    private String nombre;
    private Long orden;
    private LocalDateTime fechaCreacion;
    private Client cliente;
    private AtendidoPorDto atendidoPor;
    private Boolean perteneceUsuarioActual;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AtendidoPorDto {
        private java.util.UUID id;
        private String nombre;
    }
}
