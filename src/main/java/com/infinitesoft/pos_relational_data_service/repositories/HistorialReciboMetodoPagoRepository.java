package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboMetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface HistorialReciboMetodoPagoRepository extends JpaRepository<HistorialReciboMetodoPago, Long> {

    List<HistorialReciboMetodoPago> findByHistorialReciboId(Long historialReciboId);

    List<HistorialReciboMetodoPago> findByHistorialReciboIdIn(Collection<Long> historialReciboIds);

    @Query("SELECT h.metodoPagoId FROM HistorialReciboMetodoPago h WHERE h.historialReciboId = :historialReciboId")
    List<Long> findMetodoPagoIdsByHistorialReciboId(Long historialReciboId);

    @Modifying
    @Transactional
    @Query("DELETE FROM HistorialReciboMetodoPago h WHERE h.historialReciboId = :historialReciboId")
    void deleteByHistorialReciboId(Long historialReciboId);
}
