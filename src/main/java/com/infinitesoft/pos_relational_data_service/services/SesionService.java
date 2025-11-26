package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;

import java.util.List;

public interface SesionService {
    Sesion create(Sesion sesion);
    List<Sesion> findAll();
    Sesion findById(Long id);
    Sesion update(Long id, Sesion sesion);
    boolean delete(Long id);
}
