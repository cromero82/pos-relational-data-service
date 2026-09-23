package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Persona;

import java.util.List;

public interface PersonaService {
    Persona create(Persona persona);

    List<Persona> findAll();

    List<Persona> findActivas();

    Persona findById(Long id);

    Persona update(Long id, Persona persona);

    boolean delete(Long id);
}
