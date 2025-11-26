package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.repositories.SesionRepository;
import com.infinitesoft.pos_relational_data_service.services.SesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SesionServiceImpl implements SesionService {

    @Autowired
    private SesionRepository repository;

    @Override
    public Sesion create(Sesion sesion) {
        return repository.save(sesion);
    }

    @Override
    public List<Sesion> findAll() {
        return repository.findAll();
    }

    @Override
    public Sesion findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public Sesion update(Long id, Sesion sesion) {
        if (id == null) return null;
        Optional<Sesion> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        Sesion existing = existingOpt.get();
        existing.setCookie(sesion.getCookie());
        existing.setUltimoTicketId(sesion.getUltimoTicketId());
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
