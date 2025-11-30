package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;

import java.time.LocalDate;
import java.util.List;

public interface VentasTipoService {
    VentasTipo create(VentasTipo ventasTipo);
    List<VentasTipo> findAll();
    VentasTipo findById(Long id);
    VentasTipo update(Long id, VentasTipo ventasTipo);
    boolean delete(Long id);
    List<VentasTipo> findByFechaOrderByFechaDesc(LocalDate fecha);
    List<VentasTipo> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);
}
