package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.TipoOrigenFondos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoOrigenFondosRepository extends JpaRepository<TipoOrigenFondos, Integer> {
    Optional<TipoOrigenFondos> findByCodigo(String codigo);
}
