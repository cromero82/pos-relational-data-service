package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Cortes cuyo turno se solapa con el rango, o que se registraron dentro del rango
     * (fecha_creacion). Así Ingresos no pierde un cierre hecho hoy si fecha_ini/fecha_fin
     * salieron de los tickets con otra fecha.
     */
    @Query("SELECT c FROM CorteVenta c WHERE "
            + "(c.fechaIni <= :fin AND c.fechaFin >= :ini) "
            + "OR (c.fechaCreacion >= :ini AND c.fechaCreacion <= :fin) "
            + "ORDER BY c.fechaIni ASC, c.id ASC")
    List<CorteVenta> searchEnRango(
            @Param("ini") LocalDateTime ini,
            @Param("fin") LocalDateTime fin);

    List<CorteVenta> findByIdIn(List<Long> ids);
}
