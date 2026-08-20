package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CuentaPorCobrar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaPorCobrarRepository extends JpaRepository<CuentaPorCobrar, Long> {

    Optional<CuentaPorCobrar> findFirstByReciboIdAndEstadoInOrderByIdDesc(
            Long reciboId,
            List<String> estados
    );

    Optional<CuentaPorCobrar> findFirstByTicketIdAndEstadoInOrderByIdDesc(
            Long ticketId,
            List<String> estados
    );

    List<CuentaPorCobrar> findByEstadoInOrderByFechaOrigenDesc(List<String> estados);
}
