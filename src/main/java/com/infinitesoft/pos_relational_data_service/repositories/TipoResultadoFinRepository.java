package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.TipoResultadoFin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoResultadoFinRepository extends JpaRepository<TipoResultadoFin, Integer> {
    Optional<TipoResultadoFin> findFirstBySigla(String sigla);
}
