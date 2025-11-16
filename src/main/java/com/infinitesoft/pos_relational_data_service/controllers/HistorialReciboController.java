package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/historial-recibos")
@CrossOrigin(origins = "*")
public class HistorialReciboController {

    @Autowired
    private HistorialReciboService service;

    @PostMapping
    public ResponseEntity<HistorialRecibo> create(@RequestBody HistorialRecibo historialRecibo) {
        HistorialRecibo saved = service.create(historialRecibo);
        URI location = URI.create("/historial-recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // Special endpoint: quick add with default estado = PAGADO
    @PostMapping("/addquickRecibo")
    public ResponseEntity<HistorialRecibo> addquickRecibo(@RequestBody HistorialRecibo historialRecibo) {
        HistorialRecibo saved = service.createQuick(historialRecibo);
        URI location = URI.create("/historial-recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<HistorialRecibo> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialRecibo> findById(@PathVariable Long id) {
        HistorialRecibo found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialRecibo> update(@PathVariable Long id, @RequestBody HistorialRecibo historialRecibo) {
        HistorialRecibo updated = service.update(id, historialRecibo);
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
