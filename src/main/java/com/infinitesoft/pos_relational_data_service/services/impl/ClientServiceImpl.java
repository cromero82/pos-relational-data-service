package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.repositories.ClientRepository;
import com.infinitesoft.pos_relational_data_service.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public Client create(Client client) {
        // If you want to enforce default at app level:
        // if (client.getNombre() == null || client.getNombre().isBlank()) client.setNombre("anonimo");
        return clientRepository.save(client);
    }

    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client findById(Long id) {
        if (id == null) return null;
        return clientRepository.findById(id).orElse(null);
    }

    @Override
    public Client findByNombre(String nombre) {
        if (nombre == null) return null;
        return clientRepository.findFirstByNombreIgnoreCase(nombre).orElse(null);
    }

    @Override
    public Client update(Long id, Client client) {
        if (id == null) return null;
        Optional<Client> existingOpt = clientRepository.findById(id);
        if (existingOpt.isEmpty()) return null;
        Client existing = existingOpt.get();
        existing.setNombre(client.getNombre());
        existing.setTelefono(client.getTelefono());
        existing.setDocumento(client.getDocumento());
        return clientRepository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!clientRepository.existsById(id)) return false;
        clientRepository.deleteById(id);
        return true;
    }
}
