package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentasTipoRepository extends JpaRepository<VentasTipo, Long> {
    Optional<VentasTipo> findFirstByOrderByIdDesc();
    List<VentasTipo> findByIdGreaterThanEqual(Long id);
}
