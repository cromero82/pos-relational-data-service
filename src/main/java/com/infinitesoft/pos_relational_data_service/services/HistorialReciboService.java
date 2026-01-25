package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface HistorialReciboService {
    HistorialRecibo create(HistorialRecibo historialRecibo);
    // Special quick create: defaults estado to PAGADO
    HistorialRecibo createQuick(HistorialRecibo historialRecibo);
    List<HistorialRecibo> findAll();
    HistorialRecibo findById(Long id);
    HistorialRecibo update(Long id, HistorialRecibo historialRecibo, Long sesionId);
    boolean delete(Long id);
    BigDecimal getTotalByDate(String fecha);
    Page<HistorialRecibo> search(String fecha, Long estadoId, Pageable pageable);
    Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Pageable pageable);
    void moveToEdition(Long historialReciboId, Long sesionId);
}
