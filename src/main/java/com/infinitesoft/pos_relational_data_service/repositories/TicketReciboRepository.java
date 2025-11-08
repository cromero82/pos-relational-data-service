package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketReciboRepository extends JpaRepository<TicketRecibo, Long> {
    Optional<TicketRecibo> findFirstByTicketId(Long ticketId);
}
