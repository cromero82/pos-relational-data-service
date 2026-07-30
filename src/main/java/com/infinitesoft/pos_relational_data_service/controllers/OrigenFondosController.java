package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.OrigenFondosArbolItemDto;
import com.infinitesoft.pos_relational_data_service.dto.OrigenFondosDto;
import com.infinitesoft.pos_relational_data_service.services.OrigenFondosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/origenes-fondos")
@CrossOrigin(origins = "*")
public class OrigenFondosController {

    @Autowired
    private OrigenFondosService service;

    @GetMapping
    public List<OrigenFondosDto> findAll() {
        return service.findAllActivas();
    }

    @GetMapping("/para-egreso")
    public List<OrigenFondosDto> findParaEgreso() {
        return service.findParaEgreso();
    }

    @GetMapping("/arbol")
    public List<OrigenFondosArbolItemDto> findArbol() {
        return service.findArbol();
    }

    @GetMapping("/arbol-egreso")
    public List<OrigenFondosArbolItemDto> findArbolParaEgreso() {
        return service.findArbolParaEgreso();
    }

    @GetMapping("/{id}")
    public OrigenFondosDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }
}
