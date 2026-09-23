package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.EstadisticaAnualResponse;
import com.infinitesoft.pos_relational_data_service.dto.EstadisticaDiariaResponse;
import com.infinitesoft.pos_relational_data_service.dto.EstadisticaMensualResponse;
import com.infinitesoft.pos_relational_data_service.entities.EstadisticaFin;
import com.infinitesoft.pos_relational_data_service.services.EstadisticaFinancieraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/estadistica-financiera")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@CrossOrigin(origins = "*")
public class EstadisticaFinancieraController {

    private final EstadisticaFinancieraService estadisticaFinancieraService;

    @PostMapping
    public ResponseEntity<Map<String, String>> crear(@RequestBody Map<String, String> body) {
        String valorTiempo = body.get("valorTiempo");
        if (estadisticaFinancieraService.existeEstadistica(valorTiempo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe un registro en este periodo de tiempo"));
        }
        estadisticaFinancieraService.crearEstadisticaAsync(valorTiempo);
        return ResponseEntity.accepted().body(Map.of("mensaje", "Proceso de creación iniciado"));
    }

    @PutMapping
    public ResponseEntity<EstadisticaFin> actualizar(@RequestBody Map<String, String> body) {
        String valorTiempo = body.get("valorTiempo");
        EstadisticaFin actualizada = estadisticaFinancieraService.actualizarEstadisticaSync(valorTiempo);
        return ResponseEntity.ok(actualizada);
    }

    @GetMapping("/diaria")
    public ResponseEntity<Page<EstadisticaDiariaResponse>> listarDiaria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("valorTiempo").descending());
        return ResponseEntity.ok(estadisticaFinancieraService.listarDiarias(fechaInicio, fechaFin, pageable));
    }

    @GetMapping("/mensual")
    public ResponseEntity<List<EstadisticaMensualResponse>> listarMensual(
            @RequestParam(required = false) String mesInicio,
            @RequestParam(required = false) String mesFin) {
        return ResponseEntity.ok(estadisticaFinancieraService.listarMensuales(mesInicio, mesFin));
    }

    @GetMapping("/anual")
    public ResponseEntity<List<EstadisticaAnualResponse>> listarAnual(
            @RequestParam(required = false) String anioInicio,
            @RequestParam(required = false) String anioFin) {
        return ResponseEntity.ok(estadisticaFinancieraService.listarAnuales(anioInicio, anioFin));
    }
}
