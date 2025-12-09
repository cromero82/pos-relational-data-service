package com.infinitesoft.pos_relational_data_service.security.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthUserDto {
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private List<AuthRoleDto> roles;
}
