package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.NotaAjusteDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaAjusteDocumentoRepository extends JpaRepository<NotaAjusteDocumento, Long> {

    List<NotaAjusteDocumento> findByDocumentoVentaOrigenIdOrderByFechaHechoDesc(Long documentoVentaOrigenId);

    List<NotaAjusteDocumento> findByHistorialReciboIdOrderByFechaHechoDesc(Long historialReciboId);

    Optional<NotaAjusteDocumento> findFirstByDocumentoVentaOrigenIdAndOperacionRestauracionTrue(Long documentoVentaOrigenId);

    List<NotaAjusteDocumento> findByOperacionRestauracionTrueOrderByFechaHechoDesc();
}
