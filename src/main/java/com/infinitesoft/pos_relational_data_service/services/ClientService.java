package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Client;

import java.util.List;

public interface ClientService {
    Client create(Client client);
    List<Client> findAll();
    Client findById(Long id);
    Client update(Long id, Client client);
    boolean delete(Long id);
}
