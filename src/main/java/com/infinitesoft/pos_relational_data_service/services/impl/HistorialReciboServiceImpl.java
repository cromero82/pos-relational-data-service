package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialReciboServiceImpl implements HistorialReciboService {

    @Autowired
    private HistorialReciboRepository repository;

    @Override
    public HistorialRecibo create(HistorialRecibo historialRecibo) {
        return repository.save(historialRecibo);
    }

    @Override
    public List<HistorialRecibo> findAll() {
        return repository.findAll();
    }

    @Override
    public HistorialRecibo findById(Long id) {
        if (id == null) return null;
        Optional<HistorialRecibo> opt = repository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public HistorialRecibo update(Long id, HistorialRecibo historialRecibo) {
        if (id == null) return null;
        Optional<HistorialRecibo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        HistorialRecibo existing = existingOpt.get();
        existing.setClienteId(historialRecibo.getClienteId());
        existing.setEstadoId(historialRecibo.getEstadoId());
        existing.setMetodoPagoId(historialRecibo.getMetodoPagoId());
        existing.setTotal(historialRecibo.getTotal());
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
