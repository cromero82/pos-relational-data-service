package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.services.EgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EgresoServiceImpl implements EgresoService {

    @Autowired
    private EgresoRepository egresoRepository;

    @Override
    public Egreso create(Egreso egreso) {
        return egresoRepository.save(egreso);
    }

    @Override
    public List<Egreso> findAll() {
        return egresoRepository.findAll();
    }

    @Override
    public Page<Egreso> search(String descripcion, Long tipoEgresoId, Long proveedorId, Pageable pageable) {
        return egresoRepository.search(descripcion, tipoEgresoId, proveedorId, pageable);
    }

    @Override
    public Page<Egreso> searchDescripciones(String descripcion, Pageable pageable) {
        return egresoRepository.searchDescripciones(descripcion, pageable);
    }

    @Override
    public Egreso findById(Long id) {
        return egresoRepository.findById(id).orElse(null);
    }

    @Override
    public List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return egresoRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    public List<Egreso> findByProveedorId(Long proveedorId) {
        return egresoRepository.findByProveedorId(proveedorId);
    }

    @Override
    public List<Egreso> findByTipoEgresoId(Long tipoEgresoId) {
        return egresoRepository.findByTipoEgresoId(tipoEgresoId);
    }

    @Override
    public Egreso update(Long id, Egreso egreso) {
        if (egresoRepository.existsById(id)) {
            egreso.setId(id);
            return egresoRepository.save(egreso);
        }
        return null;
    }

    @Override
    public boolean delete(Long id) {
        if (egresoRepository.existsById(id)) {
            egresoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
