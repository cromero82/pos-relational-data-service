package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.services.SesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/sesiones")
@CrossOrigin(origins = "*")
public class SesionController {

    @Autowired
    private SesionService service;

    @PostMapping
    public ResponseEntity<Sesion> create(@RequestBody Sesion sesion) {
        Sesion saved = service.create(sesion);
        URI location = URI.create("/sesiones/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Sesion> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sesion> findById(@PathVariable Long id) {
        Sesion found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sesion> update(@PathVariable Long id, @RequestBody Sesion sesion) {
        Sesion updated = service.update(id, sesion);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
