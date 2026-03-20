package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.TipoEgreso;
import com.infinitesoft.pos_relational_data_service.repositories.TipoEgresoRepository;
import com.infinitesoft.pos_relational_data_service.services.TipoEgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoEgresoServiceImpl implements TipoEgresoService {

    @Autowired
    private TipoEgresoRepository tipoEgresoRepository;

    @Override
    public TipoEgreso create(TipoEgreso tipoEgreso) {
        return tipoEgresoRepository.save(tipoEgreso);
    }

    @Override
    public List<TipoEgreso> findAll() {
        return tipoEgresoRepository.findAll();
    }

    @Override
    public TipoEgreso findById(Long id) {
        return tipoEgresoRepository.findById(id).orElse(null);
    }

    @Override
    public TipoEgreso update(Long id, TipoEgreso tipoEgreso) {
        if (tipoEgresoRepository.existsById(id)) {
            tipoEgreso.setId(id);
            return tipoEgresoRepository.save(tipoEgreso);
        }
        return null;
    }

    @Override
    public boolean delete(Long id) {
        if (tipoEgresoRepository.existsById(id)) {
            tipoEgresoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
