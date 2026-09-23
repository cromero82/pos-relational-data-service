package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketReciboRepository extends JpaRepository<TicketRecibo, Long> {
    Optional<TicketRecibo> findFirstByTicketId(Long ticketId);
    List<TicketRecibo> findAllByTicketId(Long ticketId);
    Optional<TicketRecibo> findFirstByReciboId(Long reciboId);

    @Transactional
    @Modifying
    long deleteByReciboId(Long reciboId);
}
