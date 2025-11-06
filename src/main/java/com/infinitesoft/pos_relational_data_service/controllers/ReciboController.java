package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/recibos")
@CrossOrigin(origins = "*")
public class ReciboController {

    @Autowired
    private ReciboService reciboService;

    @PostMapping
    public ResponseEntity<Recibo> create(@RequestBody Recibo recibo) {
        Recibo saved = reciboService.create(recibo);
        URI location = URI.create("/recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Recibo> findAll() {
        return reciboService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recibo> findById(@PathVariable Long id) {
        Recibo found = reciboService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recibo> update(@PathVariable Long id, @RequestBody Recibo recibo) {
        Recibo updated = reciboService.update(id, recibo);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = reciboService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
