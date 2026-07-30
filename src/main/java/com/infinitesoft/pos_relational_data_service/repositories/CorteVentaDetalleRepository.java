package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CorteVentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorteVentaDetalleRepository extends JpaRepository<CorteVentaDetalle, Long> {
    List<CorteVentaDetalle> findByCorteVentaIdOrderByOrdenAscIdAsc(Long corteVentaId);
}
