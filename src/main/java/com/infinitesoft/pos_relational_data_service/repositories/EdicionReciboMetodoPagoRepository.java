package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.EdicionReciboMetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface EdicionReciboMetodoPagoRepository extends JpaRepository<EdicionReciboMetodoPago, Long> {

    List<EdicionReciboMetodoPago> findByEdicionReciboId(Long edicionReciboId);

    List<EdicionReciboMetodoPago> findByEdicionReciboIdIn(Collection<Long> edicionReciboIds);

    @Query("SELECT e.metodoPagoId FROM EdicionReciboMetodoPago e WHERE e.edicionReciboId = :edicionReciboId")
    List<Long> findMetodoPagoIdsByEdicionReciboId(Long edicionReciboId);

    @Modifying
    @Transactional
    @Query("DELETE FROM EdicionReciboMetodoPago e WHERE e.edicionReciboId = :edicionReciboId")
    void deleteByEdicionReciboId(Long edicionReciboId);
}
