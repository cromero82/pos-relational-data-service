package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.services.SesionService;
import com.infinitesoft.pos_relational_data_service.services.CierreUsuarioService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import com.infinitesoft.pos_relational_data_service.dto.SesionDto;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/sesiones")
@CrossOrigin(origins = "*")
@Log4j2
public class SesionController {

    @Autowired
    private SesionService service;

    @Autowired
    private CierreUsuarioService cierreUsuarioService;

    @PostMapping
    public ResponseEntity<SesionDto> create(@RequestBody SesionDto sesionDto,
                                            HttpServletRequest request) {
        log.info("Iniciando servicio SesionController - Método: create - DTO: {}", sesionDto);
        SesionDto saved = service.create(sesionDto, request);
        
        // Ejecutar rutina de cierre de manera asíncrona al crear una sesión
        log.info("Disparando cierre de usuario asíncrono tras creación de sesión");
        cierreUsuarioService.ejecutarCierre();
        
        URI location = URI.create("/sesiones/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<SesionDto> findAll(HttpServletRequest request) {
        log.info("Iniciando servicio SesionController - Método: findAll");
        return service.findAll(request);
    }

    @GetMapping("/usuario/todas")
    public List<SesionDto> findAllByUserAllStates(HttpServletRequest request) {
        log.info("Iniciando servicio SesionController - Método: findAllByUserAllStates");
        return service.findAllByUserAllStates(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SesionDto> findById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Iniciando servicio SesionController - Método: findById - ID: {}", id);
        SesionDto found = service.findById(id, request);
        if (found == null) {
            log.warn("Sesion con ID: {} no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @GetMapping("/{id}/usuario")
    public ResponseEntity<com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto> findUserInfoBySesionId(@PathVariable Long id, HttpServletRequest request) {
        log.info("Iniciando servicio SesionController - Método: findUserInfoBySesionId - SesionID: {}", id);
        com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto user = service.findUserInfoBySesionId(id, request);
        if (user == null) {
            log.warn("Información de usuario para SesionID: {} no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SesionDto> update(@PathVariable Long id, @RequestBody SesionDto sesionDto, HttpServletRequest request) {
        log.info("Iniciando servicio SesionController - Método: update - ID: {} - DTO: {}", id, sesionDto);
        SesionDto updated = service.update(id, sesionDto, request);
        if (updated == null) {
            log.warn("Fallo al actualizar Sesion con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        log.info("Iniciando servicio SesionController - Método: delete - ID: {}", id);
        boolean deleted = service.delete(id, request);
        if (!deleted) {
            log.warn("Fallo al eliminar (soft delete) Sesion con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}