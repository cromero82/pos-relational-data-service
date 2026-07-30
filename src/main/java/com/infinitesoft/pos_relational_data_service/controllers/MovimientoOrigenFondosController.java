package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.*;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero')")
@RequestMapping("/movimientos-origen-fondos")
@CrossOrigin(origins = "*")
public class MovimientoOrigenFondosController {

    @Autowired
    private MovimientoOrigenFondosService service;

    @GetMapping
    public List<MovimientoOrigenFondosDto> findByOrigen(@RequestParam Integer origenFondosId) {
        return service.findByOrigen(origenFondosId);
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/entrada-manual")
    public ResponseEntity<MovimientoOrigenFondosDto> entradaManual(@RequestBody MovimientoEntradaRequest request) {
        return ResponseEntity.ok(service.registrarEntradaManual(request));
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/prestamo")
    public ResponseEntity<MovimientoOrigenFondosDto> prestamo(@RequestBody MovimientoPrestamoRequest request) {
        return ResponseEntity.ok(service.registrarPrestamo(request));
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/traslado")
    public ResponseEntity<List<MovimientoOrigenFondosDto>> traslado(@RequestBody MovimientoTrasladoRequest request) {
        return ResponseEntity.ok(service.registrarTraslado(request));
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/ajuste")
    public ResponseEntity<MovimientoOrigenFondosDto> ajuste(@RequestBody MovimientoAjusteRequest request) {
        return ResponseEntity.ok(service.registrarAjuste(request));
    }
}
