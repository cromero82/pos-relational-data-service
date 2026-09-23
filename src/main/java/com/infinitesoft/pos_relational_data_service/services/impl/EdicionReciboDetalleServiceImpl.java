package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.EdicionReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.EdicionRecibo;
import com.infinitesoft.pos_relational_data_service.entities.EdicionReciboDetalle;
import com.infinitesoft.pos_relational_data_service.repositories.EdicionReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EdicionReciboDetalleRepository;
import com.infinitesoft.pos_relational_data_service.services.EdicionReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EdicionReciboDetalleServiceImpl implements EdicionReciboDetalleService {

    @Autowired
    private EdicionReciboDetalleRepository repository;

    @Autowired
    private EdicionReciboRepository edicionReciboRepository;

    @Override
    public EdicionReciboDetalle create(EdicionReciboDetalle edicionReciboDetalle) {
        return repository.save(edicionReciboDetalle);
    }

    @Override
    public List<EdicionReciboDetalle> findAll() {
        return repository.findAll();
    }

    @Override
    public EdicionReciboDetalle findById(Long id) {
        if (id == null) return null;
        Optional<EdicionReciboDetalle> opt = repository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<EdicionReciboDetalleDto> findByReciboIdOrHistorialReciboId(Long reciboId, Long historialReciboId) {
        Optional<EdicionRecibo> edicionReciboOpt = Optional.empty();
        if (reciboId != null) {
            edicionReciboOpt = edicionReciboRepository.findByReciboId(reciboId);
        } else if (historialReciboId != null) {
            edicionReciboOpt = edicionReciboRepository.findByHistorialReciboId(historialReciboId);
        }

        if (edicionReciboOpt.isPresent()) {
            EdicionRecibo edicionRecibo = edicionReciboOpt.get();
            return repository.findDtoByEdicionId(edicionRecibo.getId());
        }
        return Collections.emptyList();
    }

    @Override
    public EdicionReciboDetalle update(Long id, EdicionReciboDetalle edicionReciboDetalle) {
        if (id == null) return null;
        Optional<EdicionReciboDetalle> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        EdicionReciboDetalle existing = existingOpt.get();
        existing.setEdicionId(edicionReciboDetalle.getEdicionId());
        existing.setProductoId(edicionReciboDetalle.getProductoId());
        existing.setCantidad(edicionReciboDetalle.getCantidad());
        existing.setSubtotal(edicionReciboDetalle.getSubtotal());
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
