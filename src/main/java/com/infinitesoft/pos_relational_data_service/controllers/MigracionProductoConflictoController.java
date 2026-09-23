package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.MigracionProductoConflicto;
import com.infinitesoft.pos_relational_data_service.services.MigracionProductoConflictoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/migracion-producto-conflictos")
public class MigracionProductoConflictoController {

    @Autowired
    private MigracionProductoConflictoService service;

    @GetMapping
    public ResponseEntity<List<MigracionProductoConflicto>> getAll(@RequestParam(required = true) Integer migracionProductoId) {
        return ResponseEntity.ok(service.getAll(migracionProductoId));
    }

    @PostMapping
    public ResponseEntity<MigracionProductoConflicto> create(@RequestBody MigracionProductoConflicto migracionProductoConflicto) {
        return ResponseEntity.ok(service.create(migracionProductoConflicto));
    }
}
