package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleResponse;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;

import java.util.List;

public interface ReciboDetalleService {
    ReciboDetalleResponse create(ReciboDetalle detalle);
    List<ReciboDetalle> findAll();
    ReciboDetalle findById(Long id);
    List<ReciboDetalleDto> findByReciboId(Long reciboId);
    // Additional helpers for migration use-cases
    List<ReciboDetalle> findEntityListByReciboId(Long reciboId);
    long deleteByReciboId(Long reciboId);

    ReciboDetalle update(Long id, ReciboDetalle detalle);
    boolean delete(Long id);
}
