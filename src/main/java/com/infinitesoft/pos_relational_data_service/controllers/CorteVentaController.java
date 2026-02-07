package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.CorteVentaDTO;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoRequest;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoResponse;
import com.infinitesoft.pos_relational_data_service.dto.VentasTipoDTO;
import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;
import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import com.infinitesoft.pos_relational_data_service.services.CorteVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/corte-venta")
@CrossOrigin(origins = "*")
public class CorteVentaController {

    @Autowired
    private CorteVentaService service;

    @PostMapping
    public ResponseEntity<CorteVentaDTO> create(@RequestBody CorteVentaDTO dto) {
        CorteVenta saved = service.createFromDTO(dto);
        URI location = URI.create("/corte-venta/" + saved.getId());
        return ResponseEntity.created(location).body(service.convertToDTO(saved));
    }

    @GetMapping
    public List<CorteVentaDTO> findAll() {
        return service.findAll().stream()
                .map(service::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorteVentaDTO> findById(@PathVariable Long id) {
        CorteVenta found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.convertToDTO(found));
    }

    @GetMapping("/search")
    public List<CorteVentaDTO> search(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaIni,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return service.search(fechaIni, fechaFin);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorteVentaDTO> update(@PathVariable Long id, @RequestBody CorteVentaDTO dto) {
        CorteVenta entity = service.convertToEntity(dto);
        CorteVenta updated = service.update(id, entity);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.convertToDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consultar-rango")
    public ResponseEntity<CorteVentaRangoResponse> consultarRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaIni,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "false") boolean ultimoCorte,
            @RequestParam(defaultValue = "false") boolean actual) {
        
        CorteVentaRangoRequest request = CorteVentaRangoRequest.builder()
                .fechaIni(fechaIni)
                .fechaFin(fechaFin)
                .ultimoCorte(ultimoCorte)
                .actual(actual)
                .build();
        
        return ResponseEntity.ok(service.consultarRango(request));
    }
}
