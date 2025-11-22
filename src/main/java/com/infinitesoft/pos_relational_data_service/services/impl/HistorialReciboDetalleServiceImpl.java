package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.HistorialReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboDetalle;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboDetalleRepository;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistorialReciboDetalleServiceImpl implements HistorialReciboDetalleService {

    @Autowired
    private HistorialReciboDetalleRepository repository;

    @Override
    public List<HistorialReciboDetalleDto> findByReciboId(Long reciboId) {
        return repository.findDtoByReciboId(reciboId);
    }

    @Override
    public List<HistorialReciboDetalle> findEntityListByReciboId(Long reciboId) {
        return repository.findByReciboId(reciboId);
    }

    @Override
    @Transactional
    public void deleteByReciboId(Long reciboId) {
        List<HistorialReciboDetalle> toDelete = repository.findByReciboId(reciboId);
        if (toDelete != null && !toDelete.isEmpty()) {
            repository.deleteAll(toDelete);
        }
    }
}
