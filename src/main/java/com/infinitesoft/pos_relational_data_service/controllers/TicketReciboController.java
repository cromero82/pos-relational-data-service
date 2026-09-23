package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import com.infinitesoft.pos_relational_data_service.services.TicketReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/ticket-recibos")
@CrossOrigin(origins = "*")
public class TicketReciboController {

    @Autowired
    private TicketReciboService service;

    @PostMapping
    public ResponseEntity<TicketRecibo> create(@RequestBody TicketRecibo tr) {
        TicketRecibo saved = service.create(tr);
        URI location = URI.create("/ticket-recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<TicketRecibo> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketRecibo> findById(@PathVariable Long id) {
        TicketRecibo found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    // New endpoint: get by ticketId; creates Recibo and link when absent. Requires sessionId.
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<TicketRecibo> getByTicketId(@PathVariable Long ticketId,
                                                      @RequestParam(name = "sessionId", required = true) Long sessionId,
                                                      @RequestParam(name = "reciboPadreId", required = false) Long reciboPadreId) {
        TicketRecibo result = service.getOrCreateByTicketId(ticketId, sessionId, reciboPadreId);
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketRecibo> update(@PathVariable Long id, @RequestBody TicketRecibo tr) {
        TicketRecibo updated = service.update(id, tr);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
