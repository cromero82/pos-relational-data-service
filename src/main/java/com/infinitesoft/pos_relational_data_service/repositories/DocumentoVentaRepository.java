package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.DocumentoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoVentaRepository extends JpaRepository<DocumentoVenta, Long> {

    Optional<DocumentoVenta> findByHistorialReciboId(Long historialReciboId);

    List<DocumentoVenta> findByHistorialReciboIdIn(Collection<Long> historialReciboIds);

    boolean existsByHistorialReciboId(Long historialReciboId);
}
