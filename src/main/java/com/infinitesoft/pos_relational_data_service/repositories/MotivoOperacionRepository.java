package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.MotivoOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotivoOperacionRepository extends JpaRepository<MotivoOperacion, Long> {

    Optional<MotivoOperacion> findByCodigoAndActivoTrue(String codigo);
}
