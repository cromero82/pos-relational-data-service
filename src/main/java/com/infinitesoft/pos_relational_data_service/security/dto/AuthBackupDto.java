package com.infinitesoft.pos_relational_data_service.security.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthBackupDto {
    private List<AuthRoleDto> roles;
    private List<AuthUserDto> usuarios;
    private List<AuthUsuarioPerfilDto> usuarioPerfiles;
}
