package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.UsuarioPerfil;
import com.infinitesoft.pos_relational_data_service.repositories.UsuarioPerfilRepository;
import com.infinitesoft.pos_relational_data_service.services.UsuarioPerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioPerfilServiceImpl implements UsuarioPerfilService {

    @Autowired
    private UsuarioPerfilRepository usuarioPerfilRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioPerfil> findAll() {
        return usuarioPerfilRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioPerfil> findById(Long id) {
        return usuarioPerfilRepository.findById(id);
    }

    @Override
    @Transactional
    public UsuarioPerfil create(UsuarioPerfil usuarioPerfil) {
        return usuarioPerfilRepository.save(usuarioPerfil);
    }

    @Override
    @Transactional
    public UsuarioPerfil update(Long id, UsuarioPerfil usuarioPerfil) {
        return usuarioPerfilRepository.findById(id)
                .map(existing -> {
                    existing.setPersonalizacion(usuarioPerfil.getPersonalizacion());
                    return usuarioPerfilRepository.save(existing);
                }).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioPerfil> findByUsuarioId(UUID usuarioId) {
        return usuarioPerfilRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return usuarioPerfilRepository.findById(id)
                .map(existing -> {
                    usuarioPerfilRepository.delete(existing);
                    return true;
                }).orElse(false);
    }
}
