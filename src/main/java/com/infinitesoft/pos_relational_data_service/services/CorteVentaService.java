package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.BaseInicialPendienteDto;
import com.infinitesoft.pos_relational_data_service.dto.BaseInicialRequest;
import com.infinitesoft.pos_relational_data_service.dto.BaseInicialResultDto;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaDTO;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoRequest;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoResponse;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoPendienteDto;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoRequest;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoResultDto;
import com.infinitesoft.pos_relational_data_service.dto.FinalizarRevisionCorteRequest;
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
    CorteVentaDTO finalizarRevision(Long id, FinalizarRevisionCorteRequest request);
    CorteVentaRangoResponse consultarRango(CorteVentaRangoRequest request);
    List<CorteVentaDTO> search(LocalDateTime fechaIni, LocalDateTime fechaFin);
    DistribucionEfectivoPendienteDto obtenerDistribucionPendiente();
    DistribucionEfectivoResultDto confirmarDistribucionEfectivo(Long corteId, DistribucionEfectivoRequest request);
    BaseInicialPendienteDto obtenerBaseInicialPendiente();
    BaseInicialResultDto confirmarBaseInicial(BaseInicialRequest request);
    CorteVentaDTO convertToDTO(CorteVenta entity);
    CorteVenta convertToEntity(CorteVentaDTO dto);
}
