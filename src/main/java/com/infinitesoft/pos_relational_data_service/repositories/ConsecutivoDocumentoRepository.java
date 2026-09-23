package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.ConsecutivoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ConsecutivoDocumentoRepository extends JpaRepository<ConsecutivoDocumento, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ConsecutivoDocumento> findByTipoAndAnio(String tipo, Integer anio);
}
