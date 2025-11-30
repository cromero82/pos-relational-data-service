package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import com.infinitesoft.pos_relational_data_service.repositories.VentasTipoRepository;
import com.infinitesoft.pos_relational_data_service.services.VentasTipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VentasTipoServiceImpl implements VentasTipoService {

    @Autowired
    private VentasTipoRepository repository;

    @Override
    public VentasTipo create(VentasTipo ventasTipo) {
        if (repository.existsByMetodoPagoIdAndFecha(ventasTipo.getMetodoPagoId(), ventasTipo.getFecha())) {
            throw new DataIntegrityViolationException("Ya existe un registro para este método de pago en esta fecha.");
        }
        return repository.save(ventasTipo);
    }

    @Override
    public List<VentasTipo> findAll() {
        return repository.findAll();
    }

    @Override
    public VentasTipo findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public VentasTipo update(Long id, VentasTipo ventasTipo) {
        if (id == null) return null;
        Optional<VentasTipo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        VentasTipo existing = existingOpt.get();
        existing.setMetodoPagoId(ventasTipo.getMetodoPagoId());
        existing.setFecha(ventasTipo.getFecha());
        existing.setTotal(ventasTipo.getTotal());
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public List<VentasTipo> findByFechaOrderByFechaDesc(LocalDate fecha) {
        return repository.findByFechaOrderByFechaDesc(fecha);
    }

    @Override
    public List<VentasTipo> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }
}
