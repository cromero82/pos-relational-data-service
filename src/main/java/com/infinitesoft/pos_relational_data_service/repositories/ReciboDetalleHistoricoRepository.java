package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalleHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReciboDetalleHistoricoRepository extends JpaRepository<ReciboDetalleHistorico, Long> {
    long countByReciboDetalleIdAndAccion(Long reciboDetalleId, String accion);
    void deleteByReciboDetalleId(Long reciboDetalleId);
    List<ReciboDetalleHistorico> findByReciboDetalleId(Long reciboDetalleId);
}
