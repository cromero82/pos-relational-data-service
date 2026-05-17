package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.ReciboMetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReciboMetodoPagoRepository extends JpaRepository<ReciboMetodoPago, Long> {

    List<ReciboMetodoPago> findByReciboId(Long reciboId);

    List<ReciboMetodoPago> findByReciboIdIn(Collection<Long> reciboIds);

    @Query("SELECT r.metodoPagoId FROM ReciboMetodoPago r WHERE r.reciboId = :reciboId")
    List<Long> findMetodoPagoIdsByReciboId(Long reciboId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReciboMetodoPago r WHERE r.reciboId = :reciboId")
    void deleteByReciboId(Long reciboId);
}
