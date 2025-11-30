package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentasTipoRepository extends JpaRepository<VentasTipo, Long> {
    boolean existsByMetodoPagoIdAndFecha(Long metodoPagoId, LocalDate fecha);
    List<VentasTipo> findByFechaOrderByFechaDesc(LocalDate fecha);
    List<VentasTipo> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);
}
