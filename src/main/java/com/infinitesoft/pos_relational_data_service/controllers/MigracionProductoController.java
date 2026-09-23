package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.MigracionProducto;
import com.infinitesoft.pos_relational_data_service.services.MigracionProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/migracion-productos")
public class MigracionProductoController {

    @Autowired
    private MigracionProductoService service;

    @GetMapping
    public ResponseEntity<List<MigracionProducto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MigracionProducto> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nombre") String nombre) {
        return ResponseEntity.ok(service.processMigration(file, nombre));
    }
}
