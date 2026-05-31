package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialPrecioProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialPrecioProductoRepository extends JpaRepository<HistorialPrecioProducto, Long> {

    List<HistorialPrecioProducto> findByProductoIdOrderByFechaCreacionDesc(Long productoId);
}
