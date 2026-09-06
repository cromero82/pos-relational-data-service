package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.TicketDto;
import com.infinitesoft.pos_relational_data_service.dto.TicketObservacionesRequest;
import com.infinitesoft.pos_relational_data_service.dto.TicketReciboClienteRequest;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.entities.Ticket;
import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import com.infinitesoft.pos_relational_data_service.services.TicketReciboService;
import com.infinitesoft.pos_relational_data_service.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketReciboService ticketReciboService;

    @Autowired
    private ReciboService reciboService;

    @PostMapping
    public ResponseEntity<Ticket> create(@RequestBody Ticket ticket) {
        Ticket saved = ticketService.create(ticket);
        URI location = URI.create("/tickets/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Ticket> findAll() {
        return ticketService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> findById(@PathVariable Long id) {
        Ticket found = ticketService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    // New endpoint: get all tickets by sessionId
    @GetMapping("/session/{sessionId}")
    public List<TicketDto> findBySession(@PathVariable Long sessionId, HttpServletRequest request) {
        return ticketService.findBySessionIdDto(sessionId, request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> update(@PathVariable Long id, @RequestBody Ticket ticket) {
        Ticket updated = ticketService.update(id, ticket);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/observaciones")
    public ResponseEntity<Ticket> actualizarObservaciones(
            @PathVariable Long id,
            @RequestBody TicketObservacionesRequest request) {
        Ticket updated = ticketService.updateObservaciones(
                id, request != null ? request.getObservaciones() : null);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/actualizaCliente")
    public ResponseEntity<Ticket> actualizaCliente(@RequestBody TicketReciboClienteRequest request) {
        Ticket updated = ticketService.updateCliente(request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PutMapping
    public ResponseEntity<List<Ticket>> updateAll(@RequestBody List<Ticket> tickets) {
        List<Ticket> updated = ticketService.updateAll(tickets);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Ensure ticket exists before attempting cascading deletions
        Ticket ticket = ticketService.findById(id);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // 1) Find the links to recibos
        List<TicketRecibo> links = ticketReciboService.findByTicketId(id);
        for (TicketRecibo link : links) {
            Long reciboId = link.getReciboId();

            // 2) If the linked recibo is in PENDIENTE_PAGO, delete it (this handles ReciboDetalle, EdicionRecibo, and TicketRecibo links)
            if (reciboId != null) {
                Recibo recibo = reciboService.findById(reciboId);
                if (recibo != null) {
                    ReciboEstado estado = ReciboEstado.fromId(recibo.getEstadoId());
                    if (estado == ReciboEstado.PENDIENTE_PAGO) {
                        reciboService.delete(reciboId);
                    } else {
                        // If not PENDIENTE_PAGO, just delete the link for this ticket
                        ticketReciboService.delete(link.getId());
                    }
                } else {
                    // Recibo not found, just delete the link
                    ticketReciboService.delete(link.getId());
                }
            } else {
                // No recibo linked, just delete the link
                ticketReciboService.delete(link.getId());
            }
        }

        // 3) Finally delete the ticket
        boolean deleted = ticketService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
