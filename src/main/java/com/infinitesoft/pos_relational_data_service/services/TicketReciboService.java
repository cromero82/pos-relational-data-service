package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;

import java.util.List;

public interface TicketReciboService {
    TicketRecibo create(TicketRecibo tr);
    List<TicketRecibo> findAll();
    TicketRecibo findById(Long id);
    TicketRecibo update(Long id, TicketRecibo tr);
    boolean delete(Long id);

    // Find a TicketRecibo by ticketId, or create a new Recibo and link it if none exists
    TicketRecibo getOrCreateByTicketId(Long ticketId);

    // Find (non-creating) first TicketRecibo by ticketId
    TicketRecibo findFirstByTicketId(Long ticketId);

    // Find all links by ticketId (non-creating)
    List<TicketRecibo> findByTicketId(Long ticketId);
}
