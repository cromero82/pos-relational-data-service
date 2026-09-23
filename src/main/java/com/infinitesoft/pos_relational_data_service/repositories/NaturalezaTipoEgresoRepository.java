package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.NaturalezaTipoEgreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NaturalezaTipoEgresoRepository extends JpaRepository<NaturalezaTipoEgreso, Long> {
    Optional<NaturalezaTipoEgreso> findByCodigoIgnoreCase(String codigo);

    List<NaturalezaTipoEgreso> findByActivoTrueOrderByNombreAsc();

    List<NaturalezaTipoEgreso> findAllByOrderByNombreAsc();
}
