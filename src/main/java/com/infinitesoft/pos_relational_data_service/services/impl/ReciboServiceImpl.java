package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReciboServiceImpl implements ReciboService {

    @Autowired
    private ReciboRepository reciboRepository;

    @Override
    public Recibo create(Recibo recibo) {
        return reciboRepository.save(recibo);
    }

    @Override
    public List<Recibo> findAll() {
        return reciboRepository.findAll();
    }

    @Override
    public Recibo findById(Long id) {
        if (id == null) return null;
        Optional<Recibo> opt = reciboRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public Recibo update(Long id, Recibo recibo) {
        if (id == null) return null;
        Optional<Recibo> existingOpt = reciboRepository.findById(id);
        if (existingOpt.isEmpty()) return null;

        Recibo existing = existingOpt.get();
        // Update mutable fields, keep id and fechaCreacion
        existing.setClienteId(recibo.getClienteId());
        existing.setEstadoId(recibo.getEstadoId());
        existing.setMetodoPagoId(recibo.getMetodoPagoId());
        existing.setTotal(recibo.getTotal());
        return reciboRepository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!reciboRepository.existsById(id)) return false;
        reciboRepository.deleteById(id);
        return true;
    }
}
