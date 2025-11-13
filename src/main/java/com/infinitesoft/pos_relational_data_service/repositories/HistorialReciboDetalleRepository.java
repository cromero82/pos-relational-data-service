package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialReciboDetalleRepository extends JpaRepository<HistorialReciboDetalle, Long> {
    List<HistorialReciboDetalle> findByReciboId(Long reciboId);
}
