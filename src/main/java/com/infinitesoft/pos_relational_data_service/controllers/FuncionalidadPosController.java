package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.FuncionalidadPermisoDto;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthRoleDto;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.FuncionalidadPosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/funcionalidades")
@CrossOrigin(origins = "*")
public class FuncionalidadPosController {

    @Autowired
    private FuncionalidadPosService service;

    @GetMapping("/mis-permisos")
    public List<FuncionalidadPermisoDto> misPermisos() {
        AuthUserDto user = SecurityContextHelper.getUser();
        if (user == null || user.getRoles() == null) {
            return Collections.emptyList();
        }
        List<String> siglas = user.getRoles().stream()
                .map(AuthRoleDto::getSigla)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toList());
        return service.misPermisos(siglas);
    }
}
