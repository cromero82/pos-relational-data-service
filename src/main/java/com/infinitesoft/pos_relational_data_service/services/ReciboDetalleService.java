package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;

import java.util.List;

public interface ReciboDetalleService {
    ReciboDetalle create(ReciboDetalle detalle);
    List<ReciboDetalle> findAll();
    ReciboDetalle findById(Long id);
    ReciboDetalle update(Long id, ReciboDetalle detalle);
    boolean delete(Long id);
}
