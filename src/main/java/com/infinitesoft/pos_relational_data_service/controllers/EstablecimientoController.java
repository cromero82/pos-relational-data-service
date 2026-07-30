package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.EstablecimientoDto;
import com.infinitesoft.pos_relational_data_service.services.EstablecimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/establecimiento")
@CrossOrigin(origins = "*")
public class EstablecimientoController {

    @Autowired
    private EstablecimientoService establecimientoService;

    @GetMapping("/actual")
    public ResponseEntity<EstablecimientoDto> getActual() {
        return ResponseEntity.ok(establecimientoService.getActual());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<EstablecimientoDto> update(@PathVariable Long id, @RequestBody EstablecimientoDto dto) {
        return ResponseEntity.ok(establecimientoService.update(id, dto));
    }
}
