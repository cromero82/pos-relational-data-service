package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.HistorialReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboDetalle;

import java.util.List;

public interface HistorialReciboDetalleService {
    List<HistorialReciboDetalleDto> findByReciboId(Long reciboId);
    List<HistorialReciboDetalle> findEntityListByReciboId(Long reciboId);
    void deleteByReciboId(Long reciboId);
    HistorialReciboDetalle create(HistorialReciboDetalle detalle);
}
