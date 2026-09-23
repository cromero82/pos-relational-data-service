package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.EdicionRecibo;
import com.infinitesoft.pos_relational_data_service.services.EdicionReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/edicion-recibos")
@CrossOrigin(origins = "*")
public class EdicionReciboController {

    @Autowired
    private EdicionReciboService service;

    @PostMapping
    public ResponseEntity<EdicionRecibo> create(@RequestBody EdicionRecibo edicionRecibo) {
        EdicionRecibo saved = service.create(edicionRecibo);
        URI location = URI.create("/edicion-recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<EdicionRecibo> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EdicionRecibo> findById(@PathVariable Long id) {
        EdicionRecibo found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EdicionRecibo> update(@PathVariable Long id, @RequestBody EdicionRecibo edicionRecibo) {
        EdicionRecibo updated = service.update(id, edicionRecibo);
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
