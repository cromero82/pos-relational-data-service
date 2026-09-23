package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.DocumentoVentaDto;
import com.infinitesoft.pos_relational_data_service.services.DocumentoVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/documento-venta")
@CrossOrigin(origins = "*")
public class DocumentoVentaController {

    @Autowired
    private DocumentoVentaService documentoVentaService;

    @GetMapping("/by-historial/{historialReciboId}")
    public ResponseEntity<DocumentoVentaDto> byHistorial(@PathVariable Long historialReciboId) {
        return documentoVentaService.findDtoByHistorialReciboId(historialReciboId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
