package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Ticket;
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository;
import com.infinitesoft.pos_relational_data_service.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public Ticket create(Ticket ticket) {
        // nombre can be null; sessionId can reference existing Sesion (nullable allowed by DB)
        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket findById(Long id) {
        if (id == null) return null;
        Optional<Ticket> opt = ticketRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<Ticket> findBySessionId(Long sessionId) {
        if (sessionId == null) return List.of();
        return ticketRepository.findBySessionId(sessionId);
    }

    @Override
    public Ticket update(Long id, Ticket ticket) {
        if (id == null) return null;
        Optional<Ticket> existingOpt = ticketRepository.findById(id);
        if (existingOpt.isEmpty()) return null;
        Ticket existing = existingOpt.get();
        // Update mutable fields only
        existing.setNombre(ticket.getNombre());
        existing.setSessionId(ticket.getSessionId());
        // fechaCreacion is updatable=false, will be preserved
        return ticketRepository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!ticketRepository.existsById(id)) return false;
        ticketRepository.deleteById(id);
        return true;
    }
}
