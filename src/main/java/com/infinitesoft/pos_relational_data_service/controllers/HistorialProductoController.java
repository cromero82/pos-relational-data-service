package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;
import com.infinitesoft.pos_relational_data_service.services.HistorialProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/historial-productos")
@CrossOrigin(origins = "*")
public class HistorialProductoController {

    @Autowired
    private HistorialProductoService service;

    @PostMapping
    public ResponseEntity<HistorialProducto> create(@RequestBody HistorialProducto historial) {
        HistorialProducto saved = service.create(historial);
        URI location = URI.create("/historial-productos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<HistorialProducto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialProducto> findById(@PathVariable Long id) {
        HistorialProducto found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialProducto> update(@PathVariable Long id, @RequestBody HistorialProducto historial) {
        HistorialProducto updated = service.update(id, historial);
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
