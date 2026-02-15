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
public class SesionDto {
    private Long id;
    private String cookie;
    private Long ultimoTicketId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Client cliente;
    // Intentionally no userId field for data protection
}
