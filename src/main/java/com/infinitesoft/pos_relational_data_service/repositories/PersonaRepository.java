package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByDocumentoIgnoreCase(String documento);

    List<Persona> findByActivoTrueOrderByNombreAsc();

    List<Persona> findAllByOrderByNombreAsc();
}
