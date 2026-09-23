package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.TipoEgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoEgresoRepository extends JpaRepository<TipoEgreso, Long> {
    long countByNaturalezaId(Long naturalezaId);
}
