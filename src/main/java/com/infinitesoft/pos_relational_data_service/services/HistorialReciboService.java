package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;

import java.math.BigDecimal;
import java.util.List;

public interface HistorialReciboService {
    HistorialRecibo create(HistorialRecibo historialRecibo);
    // Special quick create: defaults estado to PAGADO
    HistorialRecibo createQuick(HistorialRecibo historialRecibo);
    List<HistorialRecibo> findAll();
    HistorialRecibo findById(Long id);
    HistorialRecibo update(Long id, HistorialRecibo historialRecibo);
    boolean delete(Long id);
    BigDecimal getTotalByDate(String fecha);
}
