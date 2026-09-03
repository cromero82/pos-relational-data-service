package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.CorregirMetodoPagoHreRequest;
import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboElectronico;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboElectronicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/historial-recibos-electronicos")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
public class HistorialReciboElectronicoController {

    @Autowired
    private HistorialReciboElectronicoService service;

    @PutMapping("/{id}/corregir-metodo-pago")
    @PreAuthorize("hasAnyRole('admin','cajero')")
    public ResponseEntity<?> corregirMetodoPago(
            @PathVariable Long id,
            @RequestBody CorregirMetodoPagoHreRequest body) {
        try {
            Long metodoPagoId = body != null ? body.getMetodoPagoId() : null;
            HistorialReciboElectronico updated = service.corregirMetodoPago(id, metodoPagoId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
