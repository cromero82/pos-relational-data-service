package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import com.infinitesoft.pos_relational_data_service.services.VentasTipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ventas-tipo")
@CrossOrigin(origins = "*")
public class VentasTipoController {

    @Autowired
    private VentasTipoService service;

    @PostMapping
    public ResponseEntity<VentasTipo> create(@RequestBody VentasTipo ventasTipo) {
        VentasTipo saved = service.create(ventasTipo);
        URI location = URI.create("/ventas-tipo/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<VentasTipo> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentasTipo> findById(@PathVariable Long id) {
        VentasTipo found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VentasTipo> update(@PathVariable Long id, @RequestBody VentasTipo ventasTipo) {
        VentasTipo updated = service.update(id, ventasTipo);
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

    @GetMapping("/by-fecha")
    public List<VentasTipo> findByFecha(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.findByFechaOrderByFechaDesc(fecha);
    }

    @GetMapping("/by-fecha-range")
    public List<VentasTipo> findByFechaBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return service.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }
}
