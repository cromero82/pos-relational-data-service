package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialReciboPagoRepository extends JpaRepository<HistorialReciboPago, Long> {

    List<HistorialReciboPago> findByHistorialReciboIdOrderByOrdenAsc(Long historialReciboId);

    void deleteByHistorialReciboId(Long historialReciboId);

    /**
     * Ventas pagadas (estadoId=2) agregadas por medio desde líneas de cobro.
     */
    @Query("SELECT p.metodoPagoId, SUM(p.monto) FROM HistorialReciboPago p, HistorialRecibo h "
            + "WHERE p.historialReciboId = h.id "
            + "AND h.fechaCreacion >= :start AND h.fechaCreacion <= :end "
            + "AND h.estadoId = 2 "
            + "GROUP BY p.metodoPagoId")
    List<Object[]> findResumenVentasPorMetodoPago(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT p.metodoPagoId, SUM(p.monto) FROM HistorialReciboPago p, HistorialRecibo h "
            + "WHERE p.historialReciboId = h.id "
            + "AND h.id > :afterId AND h.fechaCreacion <= :end "
            + "AND h.estadoId = 2 "
            + "GROUP BY p.metodoPagoId")
    List<Object[]> findResumenVentasPorMetodoPagoAfterId(
            @Param("afterId") Long afterId,
            @Param("end") LocalDateTime end);
}
