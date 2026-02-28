package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.UsuarioPerfil;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.UsuarioPerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/usuario-perfil")
@CrossOrigin(origins = "*")
public class UsuarioPerfilController {

    @Autowired
    private UsuarioPerfilService usuarioPerfilService;

    @GetMapping
    public List<UsuarioPerfil> getAll() {
        return usuarioPerfilService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioPerfil> getById(@PathVariable Long id) {
        return usuarioPerfilService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UsuarioPerfil> create(@RequestBody UsuarioPerfil usuarioPerfil) {
        UsuarioPerfil saved = usuarioPerfilService.create(usuarioPerfil);
        return ResponseEntity.created(URI.create("/usuario-perfil/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioPerfil> update(@PathVariable Long id, @RequestBody UsuarioPerfil usuarioPerfil) {
        UsuarioPerfil updated = usuarioPerfilService.update(id, usuarioPerfil);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<UsuarioPerfil> getByUsuarioId(@PathVariable UUID usuarioId) {
        return usuarioPerfilService.findByUsuarioId(usuarioId);
    }

    @GetMapping("/mi-perfil")
    public List<UsuarioPerfil> getMiPerfil() {
        UUID usuarioId = SecurityContextHelper.getUserId();
        return usuarioPerfilService.findByUsuarioId(usuarioId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (usuarioPerfilService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
