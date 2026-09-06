package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.AbonoCxc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AbonoCxcRepository extends JpaRepository<AbonoCxc, Long> {

    List<AbonoCxc> findByCuentaPorCobrarIdOrderByFechaAbonoDescIdDesc(Long cuentaPorCobrarId);

    List<AbonoCxc> findByCuentaPorCobrarIdOrderByFechaAbonoAscIdAsc(Long cuentaPorCobrarId);

    long countByCuentaPorCobrarId(Long cuentaPorCobrarId);

    @Query(value = "SELECT COALESCE(SUM(monto), 0) FROM abono_cxc WHERE cuenta_por_cobrar_id = :cuentaId",
            nativeQuery = true)
    BigDecimal sumMontoByCuentaPorCobrarId(@Param("cuentaId") Long cuentaId);
}
