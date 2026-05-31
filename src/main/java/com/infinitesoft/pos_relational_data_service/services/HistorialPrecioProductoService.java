package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.HistorialPrecioProducto;

import java.util.List;

public interface HistorialPrecioProductoService {

    HistorialPrecioProducto save(HistorialPrecioProducto historial);

    List<HistorialPrecioProducto> findByProductoId(Long productoId);
}
