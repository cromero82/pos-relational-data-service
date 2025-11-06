package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;

import java.util.List;

public interface HistorialProductoService {
    HistorialProducto create(HistorialProducto historial);
    List<HistorialProducto> findAll();
    HistorialProducto findById(Long id);
    HistorialProducto update(Long id, HistorialProducto historial);
    boolean delete(Long id);
}
