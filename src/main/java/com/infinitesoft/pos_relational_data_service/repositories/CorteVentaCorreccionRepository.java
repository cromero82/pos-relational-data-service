package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CorteVentaCorreccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorteVentaCorreccionRepository extends JpaRepository<CorteVentaCorreccion, Long> {
    List<CorteVentaCorreccion> findByCorteOriginalIdOrderByIdDesc(Long corteOriginalId);
    boolean existsByCorteOriginalId(Long corteOriginalId);
}
