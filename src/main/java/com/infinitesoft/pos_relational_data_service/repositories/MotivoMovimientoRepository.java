package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.MotivoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotivoMovimientoRepository extends JpaRepository<MotivoMovimiento, Integer> {
    List<MotivoMovimiento> findByActivoTrueOrderByOrdenAscIdAsc();

    Optional<MotivoMovimiento> findByCodigo(String codigo);
}
