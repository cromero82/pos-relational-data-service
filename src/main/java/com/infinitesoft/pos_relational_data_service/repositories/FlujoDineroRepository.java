package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.FlujoDinero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlujoDineroRepository extends JpaRepository<FlujoDinero, Long> {
    List<FlujoDinero> findAllByFecha(LocalDate fecha);
    List<FlujoDinero> findAllByFechaBetween(LocalDate from, LocalDate to);
    List<FlujoDinero> findAllByFechaBetweenAndTipoId(LocalDate from, LocalDate to, Integer tipoId);
}