package com.infinitesoft.pos_relational_data_service.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUsuarioPerfilDto {
    private Long id;
    private UUID usuarioId;
    private String personalizacion;
}
