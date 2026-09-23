package com.infinitesoft.pos_relational_data_service.security.dto;

import lombok.Data;

@Data
public class AuthRoleDto {
    private Long id;
    private String nombre;
    private String sigla;
}
