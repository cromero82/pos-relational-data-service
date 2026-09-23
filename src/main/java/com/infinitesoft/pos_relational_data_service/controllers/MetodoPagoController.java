package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;
import com.infinitesoft.pos_relational_data_service.services.MetodoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/metodos-pago")
@CrossOrigin(origins = "*")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoService service;

    @GetMapping
    public List<MetodoPago> findAll(
            @RequestParam(required = false) Boolean paraEgresos,
            @RequestParam(required = false) Boolean paraTickets,
            @RequestParam(required = false) Boolean paraNotificacion) {
        if (Boolean.TRUE.equals(paraEgresos)) {
            return service.findForEgresos();
        }
        if (Boolean.TRUE.equals(paraTickets)) {
            return service.findForTickets();
        }
        if (Boolean.TRUE.equals(paraNotificacion)) {
            return service.findQuePermitenNotificacion();
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPago> findById(@PathVariable Long id) {
        MetodoPago found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> create(@RequestBody MetodoPago body) {
        try {
            MetodoPago saved = service.create(body);
            return ResponseEntity.created(URI.create("/metodos-pago/" + saved.getId())).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MetodoPago body) {
        try {
            MetodoPago updated = service.update(id, body);
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
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
