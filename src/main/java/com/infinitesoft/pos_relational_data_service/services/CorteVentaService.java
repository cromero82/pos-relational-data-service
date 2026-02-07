package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.CorteVentaDTO;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoRequest;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoResponse;
import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;

import java.time.LocalDateTime;
import java.util.List;

public interface CorteVentaService {
    CorteVenta create(CorteVenta corteVenta);
    CorteVenta createFromDTO(CorteVentaDTO dto);
    List<CorteVenta> findAll();
    CorteVenta findById(Long id);
    CorteVenta update(Long id, CorteVenta corteVenta);
    boolean delete(Long id);
    CorteVentaRangoResponse consultarRango(CorteVentaRangoRequest request);
    List<CorteVentaDTO> search(LocalDateTime fechaIni, LocalDateTime fechaFin);
    CorteVentaDTO convertToDTO(CorteVenta entity);
    CorteVenta convertToEntity(CorteVentaDTO dto);
}
