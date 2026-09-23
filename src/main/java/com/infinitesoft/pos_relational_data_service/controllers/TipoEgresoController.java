package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.TipoEgreso;
import com.infinitesoft.pos_relational_data_service.services.TipoEgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/tipo_egresos")
@CrossOrigin(origins = "*")
public class TipoEgresoController {

    @Autowired
    private TipoEgresoService tipoEgresoService;

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<TipoEgreso> create(@RequestBody TipoEgreso tipoEgreso) {
        TipoEgreso saved = tipoEgresoService.create(tipoEgreso);
        URI location = URI.create("/tipo_egresos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<TipoEgreso> findAll() {
        return tipoEgresoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEgreso> findById(@PathVariable Long id) {
        TipoEgreso found = tipoEgresoService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<TipoEgreso> update(@PathVariable Long id, @RequestBody TipoEgreso tipoEgreso) {
        TipoEgreso updated = tipoEgresoService.update(id, tipoEgreso);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = tipoEgresoService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
