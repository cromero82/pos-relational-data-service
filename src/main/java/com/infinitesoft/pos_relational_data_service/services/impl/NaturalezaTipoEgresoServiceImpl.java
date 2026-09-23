package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.NaturalezaTipoEgreso;
import com.infinitesoft.pos_relational_data_service.repositories.NaturalezaTipoEgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TipoEgresoRepository;
import com.infinitesoft.pos_relational_data_service.services.NaturalezaTipoEgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NaturalezaTipoEgresoServiceImpl implements NaturalezaTipoEgresoService {

    @Autowired
    private NaturalezaTipoEgresoRepository naturalezaTipoEgresoRepository;

    @Autowired
    private TipoEgresoRepository tipoEgresoRepository;

    @Override
    public NaturalezaTipoEgreso create(NaturalezaTipoEgreso entity) {
        if (entity.getCodigo() == null || entity.getCodigo().isBlank()) {
            throw new IllegalArgumentException("codigo es obligatorio");
        }
        if (entity.getNombre() == null || entity.getNombre().isBlank()) {
            throw new IllegalArgumentException("nombre es obligatorio");
        }
        naturalezaTipoEgresoRepository.findByCodigoIgnoreCase(entity.getCodigo().trim()).ifPresent(x -> {
            throw new IllegalArgumentException("Ya existe una naturaleza con código " + entity.getCodigo());
        });
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }
        return naturalezaTipoEgresoRepository.save(entity);
    }

    @Override
    public List<NaturalezaTipoEgreso> findAll() {
        return naturalezaTipoEgresoRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public List<NaturalezaTipoEgreso> findActivas() {
        return naturalezaTipoEgresoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Override
    public NaturalezaTipoEgreso findById(Long id) {
        return naturalezaTipoEgresoRepository.findById(id).orElse(null);
    }

    @Override
    public NaturalezaTipoEgreso update(Long id, NaturalezaTipoEgreso entity) {
        NaturalezaTipoEgreso existing = findById(id);
        if (existing == null) {
            return null;
        }
        if (entity.getCodigo() != null && !entity.getCodigo().isBlank()) {
            naturalezaTipoEgresoRepository.findByCodigoIgnoreCase(entity.getCodigo().trim()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new IllegalArgumentException("Ya existe una naturaleza con código " + entity.getCodigo());
                }
            });
            existing.setCodigo(entity.getCodigo());
        }
        if (entity.getNombre() != null) {
            existing.setNombre(entity.getNombre());
        }
        existing.setDescripcion(entity.getDescripcion());
        if (entity.getActivo() != null) {
            existing.setActivo(entity.getActivo());
        }
        return naturalezaTipoEgresoRepository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (!naturalezaTipoEgresoRepository.existsById(id)) {
            return false;
        }
        long enUso = tipoEgresoRepository.countByNaturalezaId(id);
        if (enUso > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar: hay " + enUso + " tipo(s) de egreso usando esta naturaleza");
        }
        naturalezaTipoEgresoRepository.deleteById(id);
        return true;
    }
}
