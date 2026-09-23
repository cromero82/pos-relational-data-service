package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.TipoMovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMovimientoInventarioRepository extends JpaRepository<TipoMovimientoInventario, Long> {

    Optional<TipoMovimientoInventario> findByCodigoAndActivoTrue(String codigo);
}
