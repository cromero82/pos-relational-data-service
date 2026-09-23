package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<Client> create(@RequestBody Client client) {
        Client saved = clientService.create(client);
        URI location = URI.create("/clients/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Client> findAll() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> findById(@PathVariable Long id) {
        Client found = clientService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> update(@PathVariable Long id, @RequestBody Client client) {
        Client updated = clientService.update(id, client);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = clientService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
