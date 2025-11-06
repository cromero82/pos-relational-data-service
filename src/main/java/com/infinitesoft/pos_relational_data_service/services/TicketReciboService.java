package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;

import java.util.List;

public interface TicketReciboService {
    TicketRecibo create(TicketRecibo tr);
    List<TicketRecibo> findAll();
    TicketRecibo findById(Long id);
    TicketRecibo update(Long id, TicketRecibo tr);
    boolean delete(Long id);
}
