package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.services.SesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/sessions")
@CrossOrigin(origins = "*")
public class SesionController {

    @Autowired
    private SesionService sesionService;

    @PostMapping
    public ResponseEntity<Sesion> create(@RequestBody Sesion sesion) {
        Sesion saved = sesionService.create(sesion);
        URI location = URI.create("/sessions/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Sesion> findAll() {
        return sesionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sesion> findById(@PathVariable Long id) {
        Sesion found = sesionService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }
}
