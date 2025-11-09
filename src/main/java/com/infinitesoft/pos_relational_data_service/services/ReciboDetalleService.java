package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;

import java.util.List;

public interface ReciboDetalleService {
    ReciboDetalle create(ReciboDetalle detalle);
    List<ReciboDetalle> findAll();
    ReciboDetalle findById(Long id);
    List<ReciboDetalleDto> findByReciboId(Long reciboId);
    ReciboDetalle update(Long id, ReciboDetalle detalle);
    boolean delete(Long id);
}
