package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface CorteVentaRepository extends JpaRepository<CorteVenta, Long> {

    /**
     * Estados que sacan a un corte de circulación: ya no cuenta en Ingresos ni puede ser el
     * "último corte vigente". {@code dividido} es el corte original de un SPLIT: sus ventas
     * las heredan los cortes nuevos, así que contarlo otra vez las duplicaría.
     */
    List<String> ESTADOS_NO_VIGENTES = Arrays.asList("eliminado", "dividido");

    Optional<CorteVenta> findFirstByOrderByFechaCreacionDesc();

    /** Desempata por id: un SPLIT crea varios cortes con la misma fechaCreacion. */
    Optional<CorteVenta> findFirstByEstadoNotInOrderByFechaCreacionDescIdDesc(List<String> estados);

    Optional<CorteVenta> findFirstByEstadoNotInOrderByIdDesc(List<String> estados);

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
