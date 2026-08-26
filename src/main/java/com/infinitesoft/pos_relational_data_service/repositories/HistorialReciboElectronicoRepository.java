package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboElectronico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistorialReciboElectronicoRepository
        extends JpaRepository<HistorialReciboElectronico, Long> {

    Optional<HistorialReciboElectronico> findByHistorialReciboId(Long historialReciboId);

    Optional<HistorialReciboElectronico> findByAbonoCxcId(Long abonoCxcId);

    List<HistorialReciboElectronico> findByHistorialReciboIdIn(List<Long> historialReciboIds);
}
