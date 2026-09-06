package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query(value = "SELECT t.id, t.sesion_id, t.nombre, t.orden, t.fecha_creacion, r.cliente_id, t.observaciones " +
                   "FROM ticket t " +
                   "LEFT JOIN ticket_recibo tr ON t.id = tr.ticket_id " +
                   "LEFT JOIN recibo r ON tr.recibo_id = r.id AND r.cliente_id != :anonId " +
                   "WHERE t.sesion_id = :sessionId ORDER BY t.orden ASC", nativeQuery = true)
    List<Object[]> findTicketsWithNonAnonClienteBySessionId(@Param("sessionId") Long sessionId, @Param("anonId") Long anonId);

    @Query(value = "SELECT t.id, t.sesion_id, t.nombre, t.orden, t.fecha_creacion, r.cliente_id, t.observaciones " +
                   "FROM ticket t " +
                   "JOIN ticket_recibo tr ON t.id = tr.ticket_id " +
                   "JOIN recibo r ON tr.recibo_id = r.id " +
                   "WHERE r.cliente_id != :anonId AND t.sesion_id != :sessionId", nativeQuery = true)
    List<Object[]> findTicketsWithNonAnonClienteByNoSessionId(@Param("sessionId") Long sessionId, @Param("anonId") Long anonId);

    List<Ticket> findBySessionIdOrderByOrdenAsc(Long sessionId);

    @Query(value = "SELECT c.id FROM client c " +
                   "JOIN recibo r ON c.id = r.cliente_id " +
                   "JOIN ticket_recibo tr ON r.id = tr.recibo_id " +
                   "WHERE tr.ticket_id = :ticketId LIMIT 1", nativeQuery = true)
    Optional<Long> findClienteIdByTicketId(@Param("ticketId") Long ticketId);
}
