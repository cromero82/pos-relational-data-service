package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/recibo-detalles")
@CrossOrigin(origins = "*")
public class ReciboDetalleController {

    @Autowired
    private ReciboDetalleService service;

    @PostMapping
    public ResponseEntity<ReciboDetalle> create(@RequestBody ReciboDetalle detalle) {
        ReciboDetalle saved = service.create(detalle);
        URI location = URI.create("/recibo-detalles/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<ReciboDetalle> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReciboDetalle> findById(@PathVariable Long id) {
        ReciboDetalle found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReciboDetalle> update(@PathVariable Long id, @RequestBody ReciboDetalle detalle) {
        ReciboDetalle updated = service.update(id, detalle);
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
