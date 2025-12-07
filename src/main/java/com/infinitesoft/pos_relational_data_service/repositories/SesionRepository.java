package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByUserIdAndEsActivoTrue(UUID userId);
    Optional<Sesion> findByIdAndUserIdAndEsActivoTrue(Long id, UUID userId);
    List<Sesion> findByUserId(UUID userId);
    List<Sesion> findByUserIdOrderByFechaInicioDesc(UUID userId);
}
