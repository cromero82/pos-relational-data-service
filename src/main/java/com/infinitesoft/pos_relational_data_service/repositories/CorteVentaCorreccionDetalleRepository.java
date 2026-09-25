package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CorteVentaCorreccionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorteVentaCorreccionDetalleRepository extends JpaRepository<CorteVentaCorreccionDetalle, Long> {
    List<CorteVentaCorreccionDetalle> findByCorreccionIdOrderByOrdenAsc(Long correccionId);
}
