package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialReciboRepository extends JpaRepository<HistorialRecibo, Long>,
        JpaSpecificationExecutor<HistorialRecibo> {

    @Query("SELECT SUM(h.total) FROM HistorialRecibo h WHERE h.fechaCreacion BETWEEN ?1 AND ?2")
    BigDecimal sumTotalByFechaCreacionBetween(LocalDateTime start, LocalDateTime end);

    Page<HistorialRecibo> findByFechaCreacionBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<HistorialRecibo> findByEstadoId(Long estadoId, Pageable pageable);

    Page<HistorialRecibo> findByFechaCreacionBetweenAndEstadoId(LocalDateTime start, LocalDateTime end, Long estadoId, Pageable pageable);
    
    List<HistorialRecibo> findBySesionId(Long sesionId);

    List<HistorialRecibo> findBySesionIdAndEstadoId(Long sesionId, Long estadoId);

    List<HistorialRecibo> findBySesionIdAndFechaCreacionBetween(Long sesionId, LocalDateTime start, LocalDateTime end);

    List<HistorialRecibo> findBySesionIdAndFechaCreacionBetweenAndEstadoId(Long sesionId, LocalDateTime start, LocalDateTime end, Long estadoId);

    @Query("SELECT h FROM HistorialRecibo h WHERE h.fechaCreacion >= :fecha ORDER BY h.id ASC")
    List<HistorialRecibo> findAllPosteriorAFecha(LocalDateTime fecha);

    Optional<HistorialRecibo> findFirstByOrderByFechaCreacionAsc();

    Optional<HistorialRecibo> findFirstByOrderByFechaCreacionDesc();

    Optional<HistorialRecibo> findFirstByIdGreaterThanOrderByIdAsc(Long id);

    @Query("SELECT MIN(h.fechaCreacion) FROM HistorialRecibo h WHERE h.id > :afterId")
    Optional<LocalDateTime> findMinFechaCreacionByIdGreaterThan(@Param("afterId") Long afterId);

    @Query("SELECT MAX(h.fechaCreacion) FROM HistorialRecibo h WHERE h.id > :afterId")
    Optional<LocalDateTime> findMaxFechaCreacionByIdGreaterThan(@Param("afterId") Long afterId);

    @Query("SELECT h.metodoPagoId, SUM(h.total) FROM HistorialRecibo h " +
           "WHERE h.fechaCreacion >= :start AND h.fechaCreacion <= :end " +
           "AND h.estadoId = 2 " +
           "GROUP BY h.metodoPagoId")
    List<Object[]> findResumenVentasPorMetodoPago(LocalDateTime start, LocalDateTime end);

    /**
     * Ventas del turno abierto: id estrictamente mayor al watermark del último corte.
     */
    @Query("SELECT h.metodoPagoId, SUM(h.total) FROM HistorialRecibo h " +
           "WHERE h.id > :afterId AND h.fechaCreacion <= :end " +
           "AND h.estadoId = 2 " +
           "GROUP BY h.metodoPagoId")
    List<Object[]> findResumenVentasPorMetodoPagoAfterId(
            @Param("afterId") Long afterId,
            @Param("end") LocalDateTime end);

    @Query("SELECT MAX(h.id) FROM HistorialRecibo h WHERE h.fechaCreacion >= :start AND h.fechaCreacion <= :end")
    Optional<Long> findMaxIdByFechaCreacionBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT MAX(h.id) FROM HistorialRecibo h WHERE h.fechaCreacion <= :fecha")
    Optional<Long> findMaxIdByFechaCreacionLessThanEqual(LocalDateTime fecha);
}
