package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrigenFondosRepository extends JpaRepository<OrigenFondos, Integer> {

    @Query("SELECT c FROM OrigenFondos c LEFT JOIN FETCH c.tipoOrigenFondos WHERE c.activo = true ORDER BY c.orden ASC, c.id ASC")
    List<OrigenFondos> findByActivoTrueOrderByOrdenAscIdAsc();

    @Query("SELECT c FROM OrigenFondos c LEFT JOIN FETCH c.tipoOrigenFondos WHERE c.visibleEnEgreso = true AND c.activo = true ORDER BY c.orden ASC, c.id ASC")
    List<OrigenFondos> findByVisibleEnEgresoTrueAndActivoTrueOrderByOrdenAscIdAsc();

    Optional<OrigenFondos> findByMetodoPagoId(Long metodoPagoId);
}
