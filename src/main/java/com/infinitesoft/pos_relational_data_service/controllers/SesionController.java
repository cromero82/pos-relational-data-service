package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.services.SesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import javax.servlet.http.HttpServletRequest;
import com.infinitesoft.pos_relational_data_service.security.util.TokenUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.infinitesoft.pos_relational_data_service.dto.SesionDto;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/sesiones")
@CrossOrigin(origins = "*")
public class SesionController {

    @Autowired
    private SesionService service;

    @PostMapping
    public ResponseEntity<SesionDto> create(@RequestBody SesionDto sesionDto,
                                            HttpServletRequest request) {
        SesionDto saved = service.create(sesionDto, request);
        URI location = URI.create("/sesiones/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<SesionDto> findAll(HttpServletRequest request) {
        return service.findAll(request);
    }

    @GetMapping("/usuario/todas")
    public List<SesionDto> findAllByUserAllStates(HttpServletRequest request) {
        return service.findAllByUserAllStates(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SesionDto> findById(@PathVariable Long id, HttpServletRequest request) {
        SesionDto found = service.findById(id, request);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @GetMapping("/{id}/usuario")
    public ResponseEntity<com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto> findUserInfoBySesionId(@PathVariable Long id, HttpServletRequest request) {
        com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto user = service.findUserInfoBySesionId(id, request);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SesionDto> update(@PathVariable Long id, @RequestBody SesionDto sesionDto, HttpServletRequest request) {
        SesionDto updated = service.update(id, sesionDto, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        boolean deleted = service.delete(id, request);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // Mapping now handled inside service layer per requirements
}
