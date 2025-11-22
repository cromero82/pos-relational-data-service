package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.dto.EdicionReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.EdicionReciboDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdicionReciboDetalleRepository extends JpaRepository<EdicionReciboDetalle, Long> {
    List<EdicionReciboDetalle> findByEdicionId(Long edicionId);

    @Query("SELECT new com.infinitesoft.pos_relational_data_service.dto.EdicionReciboDetalleDto(d.id, d.edicionId, d.productoId, d.cantidad, d.subtotal, p) " +
           "FROM EdicionReciboDetalle d JOIN Product p ON d.productoId = p.id WHERE d.edicionId = :edicionId")
    List<EdicionReciboDetalleDto> findDtoByEdicionId(@Param("edicionId") Long edicionId);
}
