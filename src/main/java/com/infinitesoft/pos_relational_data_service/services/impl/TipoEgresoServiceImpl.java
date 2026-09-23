package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.NaturalezaTipoEgreso;
import com.infinitesoft.pos_relational_data_service.entities.TipoEgreso;
import com.infinitesoft.pos_relational_data_service.repositories.NaturalezaTipoEgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TipoEgresoRepository;
import com.infinitesoft.pos_relational_data_service.services.TipoEgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoEgresoServiceImpl implements TipoEgresoService {

    @Autowired
    private TipoEgresoRepository tipoEgresoRepository;

    @Autowired
    private NaturalezaTipoEgresoRepository naturalezaTipoEgresoRepository;

    @Override
    public TipoEgreso create(TipoEgreso tipoEgreso) {
        resolverNaturaleza(tipoEgreso);
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
        if (!tipoEgresoRepository.existsById(id)) {
            return null;
        }
        tipoEgreso.setId(id);
        resolverNaturaleza(tipoEgreso);
        return tipoEgresoRepository.save(tipoEgreso);
    }

    @Override
    public boolean delete(Long id) {
        if (tipoEgresoRepository.existsById(id)) {
            tipoEgresoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void resolverNaturaleza(TipoEgreso tipoEgreso) {
        if (tipoEgreso.getNaturaleza() == null || tipoEgreso.getNaturaleza().getId() == null) {
            throw new IllegalArgumentException("Debe indicar la naturaleza del tipo de egreso");
        }
        NaturalezaTipoEgreso nat = naturalezaTipoEgresoRepository
                .findById(tipoEgreso.getNaturaleza().getId())
                .orElseThrow(() -> new IllegalArgumentException("Naturaleza de tipo de egreso no encontrada"));
        if (Boolean.FALSE.equals(nat.getActivo())) {
            throw new IllegalArgumentException("La naturaleza seleccionada no está activa");
        }
        tipoEgreso.setNaturaleza(nat);
    }
}
