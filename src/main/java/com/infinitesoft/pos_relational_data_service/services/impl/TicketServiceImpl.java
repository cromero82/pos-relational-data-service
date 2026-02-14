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
        if (ticket.getSessionId() != null) {
            List<Ticket> existingTickets = findBySessionId(ticket.getSessionId());
            long maxOrden = 0L;
            if (!existingTickets.isEmpty()) {
                maxOrden = existingTickets.stream()
                        .mapToLong(Ticket::getOrden)
                        .max()
                        .orElse(0L);
            }
            ticket.setOrden(maxOrden + 1);
        } else if (ticket.getOrden() == null) {
            ticket.setOrden(1L);
        }
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
        return ticketRepository.findBySessionIdOrderByOrdenAsc(sessionId);
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
        existing.setOrden(ticket.getOrden());
        // fechaCreacion is updatable=false, will be preserved
        return ticketRepository.save(existing);
    }

    @Override
    public List<Ticket> updateAll(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) return List.of();
        // For simplicity and to reuse the logic of update(id, ticket), we can iterate.
        // However, if performance is a concern for large lists, saveAll is better.
        // But update(id, ticket) ensures only specific fields are updated and fechaCreacion is preserved.
        // Since we are receiving full Ticket objects in the request as per example, 
        // we should probably fetch and update each to be safe.
        
        return tickets.stream()
                .map(t -> update(t.getId(), t))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!ticketRepository.existsById(id)) return false;
        ticketRepository.deleteById(id);
        return true;
    }
}
