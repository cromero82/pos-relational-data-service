package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.CargueProductoConflicto;
import com.infinitesoft.pos_relational_data_service.services.CargueProductoConflictoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargue-producto-conflictos")
public class CargueProductoConflictoController {

    @Autowired
    private CargueProductoConflictoService service;

    @GetMapping
    public ResponseEntity<List<CargueProductoConflicto>> getAll(
            @RequestParam(required = true) Integer cargueProductoId,
            @RequestParam(required = false) Boolean unicamenteNoResueltos) {
        return ResponseEntity.ok(service.getAll(cargueProductoId, unicamenteNoResueltos));
    }

    @PostMapping
    public ResponseEntity<CargueProductoConflicto> create(@RequestBody CargueProductoConflicto cargueProductoConflicto) {
        return ResponseEntity.ok(service.create(cargueProductoConflicto));
    }

    @PutMapping("/{id}/resolver")
    public ResponseEntity<CargueProductoConflicto> resolverConflicto(@PathVariable Integer id) {
        return ResponseEntity.ok(service.resolverConflicto(id));
    }
}
