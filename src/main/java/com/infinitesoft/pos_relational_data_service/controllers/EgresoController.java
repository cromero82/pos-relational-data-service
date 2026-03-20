package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.services.EgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/egresos")
@CrossOrigin(origins = "*")
public class EgresoController {

    @Autowired
    private EgresoService egresoService;

    @PostMapping
    public ResponseEntity<Egreso> create(@RequestBody Egreso egreso) {
        Egreso saved = egresoService.create(egreso);
        URI location = URI.create("/egresos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Egreso> findAll() {
        return egresoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Egreso> findById(@PathVariable Long id) {
        Egreso found = egresoService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @GetMapping("/rango_fechas")
    public List<Egreso> findByFechaBetween(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return egresoService.findByFechaBetween(fechaInicio, fechaFin);
    }

    @GetMapping("/proveedor/{proveedorId}")
    public List<Egreso> findByProveedorId(@PathVariable Long proveedorId) {
        return egresoService.findByProveedorId(proveedorId);
    }

    @GetMapping("/tipo_egreso/{tipoEgresoId}")
    public List<Egreso> findByTipoEgresoId(@PathVariable Long tipoEgresoId) {
        return egresoService.findByTipoEgresoId(tipoEgresoId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Egreso> update(@PathVariable Long id, @RequestBody Egreso egreso) {
        Egreso updated = egresoService.update(id, egreso);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = egresoService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
