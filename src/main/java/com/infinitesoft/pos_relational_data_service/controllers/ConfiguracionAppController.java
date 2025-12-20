package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;
import com.infinitesoft.pos_relational_data_service.services.ConfiguracionAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/configuracion-app")
@CrossOrigin(origins = "*")
public class ConfiguracionAppController {

    @Autowired
    private ConfiguracionAppService service;

    // Endpoint: obtenerTodos
    @GetMapping("/obtenerTodos")
    public List<ConfiguracionApp> obtenerTodos() {
        return service.findAll();
    }

    // Endpoint: modificar (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracionApp> modificar(@PathVariable Long id, @RequestBody ConfiguracionApp body) {
        ConfiguracionApp updated = service.update(id, body);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}
