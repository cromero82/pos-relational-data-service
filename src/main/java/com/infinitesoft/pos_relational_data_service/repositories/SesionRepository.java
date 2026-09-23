package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT c.id FROM client c " +
                   "JOIN recibo r ON c.id = r.cliente_id " +
                   "JOIN ticket_recibo tr ON r.id = tr.recibo_id " +
                   "JOIN sesion s ON tr.ticket_id = s.ultimo_ticket_id " +
                   "WHERE s.id = :sesionId LIMIT 1", nativeQuery = true)
    Optional<Long> findClienteIdBySesionId(@Param("sesionId") Long sesionId);
}
