package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.EdicionRecibo;
import com.infinitesoft.pos_relational_data_service.repositories.EdicionReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.EdicionReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EdicionReciboServiceImpl implements EdicionReciboService {

    @Autowired
    private EdicionReciboRepository repository;

    @Override
    public EdicionRecibo create(EdicionRecibo edicionRecibo) {
        return repository.save(edicionRecibo);
    }

    @Override
    public List<EdicionRecibo> findAll() {
        return repository.findAll();
    }

    @Override
    public EdicionRecibo findById(Long id) {
        if (id == null) return null;
        Optional<EdicionRecibo> opt = repository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public Optional<EdicionRecibo> findByReciboId(Long reciboId) {
        return repository.findByReciboId(reciboId);
    }

    @Override
    public void deleteByReciboId(Long reciboId) {
        repository.findByReciboId(reciboId).ifPresent(edicion -> repository.delete(edicion));
    }

    @Override
    public EdicionRecibo update(Long id, EdicionRecibo edicionRecibo) {
        if (id == null) return null;
        Optional<EdicionRecibo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        EdicionRecibo existing = existingOpt.get();
        existing.setReciboId(edicionRecibo.getReciboId());
        existing.setClienteId(edicionRecibo.getClienteId());
        existing.setEstadoId(edicionRecibo.getEstadoId());
        existing.setMetodoPagoId(edicionRecibo.getMetodoPagoId());
        existing.setSesionId(edicionRecibo.getSesionId());
        existing.setTotal(edicionRecibo.getTotal());
        existing.setMontoRecibido(edicionRecibo.getMontoRecibido());
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
