package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Evento;
import java.util.List;
import java.util.Optional;

public interface EventoService {
    List<Evento> findAll();
    Optional<Evento> findById(Integer id);
    Optional<Evento> findBySigla(String sigla);
    Evento save(Evento evento);
    void deleteById(Integer id);
}
