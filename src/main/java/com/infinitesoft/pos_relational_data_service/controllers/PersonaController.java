package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.Persona;
import com.infinitesoft.pos_relational_data_service.services.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/personas")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    @GetMapping
    public List<Persona> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        return soloActivas ? personaService.findActivas() : personaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Persona> findById(@PathVariable Long id) {
        Persona found = personaService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> create(@RequestBody Persona body) {
        try {
            Persona saved = personaService.create(body);
            return ResponseEntity.created(URI.create("/personas/" + saved.getId())).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Persona body) {
        try {
            Persona updated = personaService.update(id, body);
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
            if (!personaService.delete(id)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }
}
