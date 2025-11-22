package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.EdicionRecibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EdicionReciboRepository extends JpaRepository<EdicionRecibo, Long> {
    Optional<EdicionRecibo> findByReciboId(Long reciboId);
    Optional<EdicionRecibo> findByHistorialReciboId(Long historialReciboId);
}
