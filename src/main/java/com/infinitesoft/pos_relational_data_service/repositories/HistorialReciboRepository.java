package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface HistorialReciboRepository extends JpaRepository<HistorialRecibo, Long> {

    @Query("SELECT SUM(h.total) FROM HistorialRecibo h WHERE h.fechaCreacion BETWEEN ?1 AND ?2")
    BigDecimal sumTotalByFechaCreacionBetween(LocalDateTime start, LocalDateTime end);
}
