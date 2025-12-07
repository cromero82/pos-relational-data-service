package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.FlujoDinero;
import com.infinitesoft.pos_relational_data_service.services.FlujoDineroService;
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
@RequestMapping("/flujos-dinero")
@CrossOrigin(origins = "*")
public class FlujoDineroController {

    @Autowired
    private FlujoDineroService service;

    @PostMapping
    public ResponseEntity<FlujoDinero> create(@RequestBody FlujoDinero flujo) {
        FlujoDinero saved = service.create(flujo);
        URI location = URI.create("/flujos-dinero/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
        
    }

    @GetMapping
    public List<FlujoDinero> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlujoDinero> findById(@PathVariable Long id) {
        FlujoDinero found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlujoDinero> update(@PathVariable Long id, @RequestBody FlujoDinero flujo) {
        FlujoDinero updated = service.update(id, flujo);
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

    // Query by exact date
    @GetMapping("/by-date")
    public List<FlujoDinero> findByFecha(@RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.findByFecha(fecha);
    }

    // Query by date range (inclusive)
    @GetMapping("/range")
    public List<FlujoDinero> findByFechaBetween(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                @RequestParam("tipoId") Integer tipoId) {
        return service.findByFechaBetween(from, to, tipoId);
    }

    // New: search by string dates (yyyy-MM-dd)
    @GetMapping("/searchAllDates")
    public List<FlujoDinero> searchAllDates(@RequestParam("dateInit") String dateInit,
                                            @RequestParam("dateEnd") String dateEnd,
                                            @RequestParam("tipoId") Integer tipoId) {
        return service.searchAllDates(dateInit, dateEnd, tipoId);
    }

    // New: search by weeks or months (e.g., 4x for 4 weeks, 2m for 2 months)
    @GetMapping("/searchByWeeks")
    public List<FlujoDinero> searchByWeeks(@RequestParam("interval") String interval, @RequestParam("tipoId") Integer tipoId) {
        return service.searchByWeeks(interval, tipoId);
    }

    @PostMapping("/generate-ingreso")
    public ResponseEntity<FlujoDinero> generateIngresoFromHistorial(@RequestParam("fecha") String fecha) {
        FlujoDinero saved = service.generateIngresoFromHistorial(fecha);
        if (saved == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(saved);
    }
}
