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
    private SesionRepository sesionRepository;

    @Override
    public Sesion create(Sesion sesion) {
        return sesionRepository.save(sesion);
    }

    @Override
    public List<Sesion> findAll() {
        return sesionRepository.findAll();
    }

    @Override
    public Sesion findById(Long id) {
        if (id == null) return null;
        Optional<Sesion> opt = sesionRepository.findById(id);
        return opt.orElse(null);
    }
}
