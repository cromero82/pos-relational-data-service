package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.UsuarioPerfil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioPerfilService {
    List<UsuarioPerfil> findAll();
    Optional<UsuarioPerfil> findById(Long id);
    UsuarioPerfil create(UsuarioPerfil usuarioPerfil);
    UsuarioPerfil update(Long id, UsuarioPerfil usuarioPerfil);
    boolean delete(Long id);
    List<UsuarioPerfil> findByUsuarioId(UUID usuarioId);
}
