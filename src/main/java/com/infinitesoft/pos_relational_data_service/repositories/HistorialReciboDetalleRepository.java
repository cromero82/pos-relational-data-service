package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.dto.HistorialReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialReciboDetalleRepository extends JpaRepository<HistorialReciboDetalle, Long> {
    List<HistorialReciboDetalle> findByReciboId(Long reciboId);

    @Query("SELECT new com.infinitesoft.pos_relational_data_service.dto.HistorialReciboDetalleDto(d.id, d.reciboId, d.productoId, d.cantidad, d.subtotal, p) " +
           "FROM HistorialReciboDetalle d JOIN d.producto p WHERE d.reciboId = :reciboId")
    List<HistorialReciboDetalleDto> findDtoByReciboId(@Param("reciboId") Long reciboId);
}
