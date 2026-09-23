package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Ticket;
import com.infinitesoft.pos_relational_data_service.dto.TicketDto;
import com.infinitesoft.pos_relational_data_service.dto.TicketReciboClienteRequest;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

public interface TicketService {
    Ticket create(Ticket ticket);
    List<Ticket> findAll();
    Ticket findById(Long id);
    List<Ticket> findBySessionId(Long sessionId);
    List<TicketDto> findBySessionIdDto(Long sessionId, HttpServletRequest request);
    Ticket update(Long id, Ticket ticket);
    Ticket updateObservaciones(Long id, String observaciones);
    Ticket updateCliente(TicketReciboClienteRequest request);
    List<Ticket> updateAll(List<Ticket> tickets);
    boolean delete(Long id);
}
