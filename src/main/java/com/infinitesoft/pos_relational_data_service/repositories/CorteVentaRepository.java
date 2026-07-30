package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CorteVentaRepository extends JpaRepository<CorteVenta, Long> {
    Optional<CorteVenta> findFirstByOrderByFechaCreacionDesc();
    Optional<CorteVenta> findFirstByEstadoNotOrderByFechaCreacionDesc(String estado);
    Optional<CorteVenta> findFirstByEstadoNotOrderByIdDesc(String estado);

    List<CorteVenta> findByFechaIniBetweenOrFechaFinBetween(
            LocalDateTime rangeStart1, LocalDateTime rangeEnd1,
            LocalDateTime rangeStart2, LocalDateTime rangeEnd2);

    List<CorteVenta> findByFechaIniGreaterThanEqualAndFechaIniLessThanEqual(LocalDateTime start, LocalDateTime end);
}
