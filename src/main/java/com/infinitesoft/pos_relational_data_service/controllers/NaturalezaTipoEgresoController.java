package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.NaturalezaTipoEgreso;
import com.infinitesoft.pos_relational_data_service.services.NaturalezaTipoEgresoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/naturaleza_tipo_egresos")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@Slf4j
public class NaturalezaTipoEgresoController {

    @Autowired
    private NaturalezaTipoEgresoService naturalezaTipoEgresoService;

    @GetMapping
    public List<NaturalezaTipoEgreso> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        return soloActivas
                ? naturalezaTipoEgresoService.findActivas()
                : naturalezaTipoEgresoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NaturalezaTipoEgreso> findById(@PathVariable Long id) {
        NaturalezaTipoEgreso found = naturalezaTipoEgresoService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> create(@RequestBody NaturalezaTipoEgreso body) {
        try {
            NaturalezaTipoEgreso saved = naturalezaTipoEgresoService.create(body);
            URI location = URI.create("/naturaleza_tipo_egresos/" + saved.getId());
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody NaturalezaTipoEgreso body) {
        try {
            NaturalezaTipoEgreso updated = naturalezaTipoEgresoService.update(id, body);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            boolean deleted = naturalezaTipoEgresoService.delete(id);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }
}
