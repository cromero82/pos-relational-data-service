package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Ticket;

import java.util.List;

public interface TicketService {
    Ticket create(Ticket ticket);
    List<Ticket> findAll();
    Ticket findById(Long id);
    Ticket update(Long id, Ticket ticket);
    boolean delete(Long id);
}
