package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.EdicionReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.EdicionReciboDetalle;

import java.util.List;

public interface EdicionReciboDetalleService {
    EdicionReciboDetalle create(EdicionReciboDetalle edicionReciboDetalle);
    List<EdicionReciboDetalle> findAll();
    EdicionReciboDetalle findById(Long id);
    List<EdicionReciboDetalleDto> findByReciboIdOrHistorialReciboId(Long reciboId, Long historialReciboId);
    EdicionReciboDetalle update(Long id, EdicionReciboDetalle edicionReciboDetalle);
    boolean delete(Long id);
}
