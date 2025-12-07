package com.infinitesoft.pos_relational_data_service.dto;

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
    // Intentionally no userId field for data protection
}
