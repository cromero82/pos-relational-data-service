package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.EstadoRecibo;

import java.util.List;

public interface EstadoReciboService {
    List<EstadoRecibo> findAll();
}
