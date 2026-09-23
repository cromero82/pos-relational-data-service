package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.FlujoDinero;

import java.time.LocalDate;
import java.util.List;

public interface FlujoDineroService {
    FlujoDinero create(FlujoDinero flujo);
    List<FlujoDinero> findAll();
    FlujoDinero findById(Long id);
    FlujoDinero update(Long id, FlujoDinero flujo);
    boolean delete(Long id);

    List<FlujoDinero> findByFecha(LocalDate fecha);
    List<FlujoDinero> findByFechaBetween(LocalDate from, LocalDate to, Integer tipoId);

    // New operations
    List<FlujoDinero> searchAllDates(String dateInit, String dateEnd, Integer tipoId);

    /**
     * interval supports formats like "6d" (6 days), "4w" (4 weeks) or "2m" (2 months). Case-insensitive.
     */
    List<FlujoDinero> searchByWeeks(String interval, Integer tipoId);

    FlujoDinero generateIngresoFromHistorial(String fecha);
}
