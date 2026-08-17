package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.HistorialDocumentosDto;
import com.infinitesoft.pos_relational_data_service.dto.MotivoOperacionRequestDto;
import com.infinitesoft.pos_relational_data_service.dto.RestaurarTicketResponseDto;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface HistorialReciboService {
    HistorialRecibo create(HistorialRecibo historialRecibo);
    HistorialRecibo createQuick(HistorialRecibo historialRecibo);
    List<HistorialRecibo> findAll();
    HistorialRecibo findById(Long id);
    HistorialRecibo update(Long id, HistorialRecibo historialRecibo, Long sesionId);
    boolean delete(Long id);
    BigDecimal getTotalByDate(String fecha);
    Page<HistorialRecibo> search(String fecha, Long estadoId, Pageable pageable);
    Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Pageable pageable);
    Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Boolean soloRestaurados, Pageable pageable);
    void moveToEdition(Long historialReciboId, Long sesionId);
    HistorialDocumentosDto getDocumentos(Long historialReciboId);
    RestaurarTicketResponseDto restaurarTicket(Long historialReciboId, Long sesionId, MotivoOperacionRequestDto request);
    java.util.List<com.infinitesoft.pos_relational_data_service.dto.ReciboPagoLineaDto> findPagosByHistorialId(Long historialReciboId);
}
