package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboDetalleRepository;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReciboDetalleServiceImpl implements ReciboDetalleService {

    @Autowired
    private ReciboDetalleRepository repository;

    @Override
    public ReciboDetalle create(ReciboDetalle detalle) {
        return repository.save(detalle);
    }

    @Override
    public List<ReciboDetalle> findAll() {
        return repository.findAll();
    }

    @Override
    public ReciboDetalle findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<ReciboDetalleDto> findByReciboId(Long reciboId) {
        if (reciboId == null) return List.of();
        return repository.findDtoByReciboId(reciboId);
    }

    @Override
    public ReciboDetalle update(Long id, ReciboDetalle detalle) {
        if (id == null) return null;
        Optional<ReciboDetalle> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        ReciboDetalle existing = existingOpt.get();
        existing.setReciboId(detalle.getReciboId());
        existing.setProductoId(detalle.getProductoId());
        existing.setCantidad(detalle.getCantidad());
        existing.setSubtotal(detalle.getSubtotal());
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
