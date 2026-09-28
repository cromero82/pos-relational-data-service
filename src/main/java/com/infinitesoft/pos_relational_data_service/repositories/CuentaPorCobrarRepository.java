package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CuentaPorCobrar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaPorCobrarRepository extends JpaRepository<CuentaPorCobrar, Long> {

    boolean existsByHistorialReciboId(Long historialReciboId);

    Optional<CuentaPorCobrar> findFirstByReciboIdAndEstadoInOrderByIdDesc(
            Long reciboId,
            List<String> estados
    );

    Optional<CuentaPorCobrar> findFirstByTicketIdAndEstadoInOrderByIdDesc(
            Long ticketId,
            List<String> estados
    );

    List<CuentaPorCobrar> findByEstadoInOrderByFechaOrigenDesc(List<String> estados);

    @Query("SELECT COALESCE(SUM(c.saldoPendiente), 0) FROM CuentaPorCobrar c "
            + "WHERE c.estado IN ('ABIERTA', 'PARCIAL')")
    BigDecimal sumSaldoVigente();

    /** Abonado de CxC vigentes: total ticket (o original) menos saldo. */
    @Query("SELECT COALESCE(SUM(CASE WHEN COALESCE(c.totalTicket, c.montoOriginal) "
            + "> c.saldoPendiente THEN COALESCE(c.totalTicket, c.montoOriginal) "
            + "- c.saldoPendiente ELSE 0 END), 0) FROM CuentaPorCobrar c "
            + "WHERE c.estado IN ('ABIERTA', 'PARCIAL')")
    BigDecimal sumCobradaVigente();

    /**
     * Suelta FK a recibo/ticket en BD de inmediato (evita 23503 al borrar recibo/ticket).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE cuenta_por_cobrar SET recibo_id = NULL, ticket_id = NULL WHERE id = :id",
            nativeQuery = true)
    int detachReciboYTicket(@Param("id") Long id);
}
