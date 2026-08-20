package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.*;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /**
     * Para «Agregar egreso» cuando el OF padre tiene bolsa destino en plantillas:
     * busca movimiento por identificar con el mismo valor, aún no formalizado.
     */
    @GetMapping("/candidatos-formalizar-egreso")
    public List<MovimientoOrigenFondosDto> candidatosFormalizarEgreso(
            @RequestParam List<Integer> origenFondosIds,
            @RequestParam BigDecimal valor
    ) {
        return service.findCandidatosFormalizarEgreso(origenFondosIds, valor);
    }

    /**
     * Reporte operativo por clasificación (piernas con impacto &gt; 0).
     * {@code clasificacionOperativa} opcional: si falta, todas las clasificadas.
     */
    @GetMapping("/por-clasificacion")
    public List<MovimientoOrigenFondosDto> findPorClasificacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String clasificacionOperativa
    ) {
        return service.findPorClasificacion(clasificacionOperativa, desde, hasta);
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
