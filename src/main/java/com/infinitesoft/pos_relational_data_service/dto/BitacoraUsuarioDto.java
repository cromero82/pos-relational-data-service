package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Evento;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BitacoraUsuarioDto {
    private Integer id;
    private UUID userId;
    private Evento evento;
    private String valorAntes;
    private String valorDespues;
    private LocalDateTime fechaCreacion;
    private AuthUserDto usuario;
}
