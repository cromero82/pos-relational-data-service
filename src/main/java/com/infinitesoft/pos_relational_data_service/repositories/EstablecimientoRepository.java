package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Long> {

    Optional<Establecimiento> findFirstByActivoTrueOrderByIdAsc();
}
