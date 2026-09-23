package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.EstadoRecibo;
import com.infinitesoft.pos_relational_data_service.repositories.EstadoReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.EstadoReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadoReciboServiceImpl implements EstadoReciboService {

    @Autowired
    private EstadoReciboRepository repository;

    @Override
    public List<EstadoRecibo> findAll() {
        return repository.findAll();
    }
}
