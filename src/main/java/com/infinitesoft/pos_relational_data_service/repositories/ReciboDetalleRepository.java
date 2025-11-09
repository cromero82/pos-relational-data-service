package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReciboDetalleRepository extends JpaRepository<ReciboDetalle, Long> {
    List<ReciboDetalle> findByReciboId(Long reciboId);

    @Query("select new com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto(d.id, d.reciboId, d.productoId, d.cantidad, d.subtotal, p) " +
           "from ReciboDetalle d join d.producto p where d.reciboId = :reciboId")
    List<ReciboDetalleDto> findDtoByReciboId(@Param("reciboId") Long reciboId);
}
