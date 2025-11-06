package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import com.infinitesoft.pos_relational_data_service.repositories.TicketReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.TicketReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketReciboServiceImpl implements TicketReciboService {

    @Autowired
    private TicketReciboRepository repository;

    @Override
    public TicketRecibo create(TicketRecibo tr) {
        return repository.save(tr);
    }

    @Override
    public List<TicketRecibo> findAll() {
        return repository.findAll();
    }

    @Override
    public TicketRecibo findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public TicketRecibo update(Long id, TicketRecibo tr) {
        if (id == null) return null;
        Optional<TicketRecibo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        TicketRecibo existing = existingOpt.get();
        existing.setTicketId(tr.getTicketId());
        existing.setReciboId(tr.getReciboId());
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
