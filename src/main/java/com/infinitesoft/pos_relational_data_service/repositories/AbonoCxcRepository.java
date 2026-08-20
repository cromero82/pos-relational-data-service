package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.AbonoCxc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbonoCxcRepository extends JpaRepository<AbonoCxc, Long> {

    List<AbonoCxc> findByCuentaPorCobrarIdOrderByFechaAbonoDescIdDesc(Long cuentaPorCobrarId);

    List<AbonoCxc> findByCuentaPorCobrarIdOrderByFechaAbonoAscIdAsc(Long cuentaPorCobrarId);

    long countByCuentaPorCobrarId(Long cuentaPorCobrarId);
}
