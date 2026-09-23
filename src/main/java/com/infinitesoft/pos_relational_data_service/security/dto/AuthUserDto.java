package com.infinitesoft.pos_relational_data_service.security.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AuthUserDto {
    private UUID id;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private List<AuthRoleDto> roles;
}
