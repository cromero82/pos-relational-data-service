package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.CargueProducto;
import com.infinitesoft.pos_relational_data_service.services.CargueProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cargue-productos")
public class CargueProductoController {

    @Autowired
    private CargueProductoService service;

    @GetMapping
    public ResponseEntity<List<CargueProducto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CargueProducto> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nombre") String nombre) {
        return ResponseEntity.ok(service.processCargue(file, nombre));
    }
}
