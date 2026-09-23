package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.EstadisticaFin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadisticaFinRepository extends JpaRepository<EstadisticaFin, Integer> {
    Optional<EstadisticaFin> findByValorTiempoAndFormatoTiempo(String valorTiempo, String formatoTiempo);
    Page<EstadisticaFin> findByFormatoTiempo(String formatoTiempo, Pageable pageable);
    List<EstadisticaFin> findByFormatoTiempo(String formatoTiempo);

    @Query("SELECT e FROM EstadisticaFin e WHERE e.formatoTiempo = :formato " +
           "AND (:inicio IS NULL OR e.valorTiempo >= :inicio) " +
           "AND (:fin IS NULL OR e.valorTiempo <= :fin)")
    Page<EstadisticaFin> findByFormatoTiempoWithRange(@Param("formato") String formato,
                                                     @Param("inicio") String inicio,
                                                     @Param("fin") String fin,
                                                     Pageable pageable);

    @Query("SELECT e FROM EstadisticaFin e WHERE e.formatoTiempo = :formato " +
           "AND (:inicio IS NULL OR e.valorTiempo >= :inicio) " +
           "AND (:fin IS NULL OR e.valorTiempo <= :fin)")
    List<EstadisticaFin> findByFormatoTiempoWithRangeList(@Param("formato") String formato,
                                                        @Param("inicio") String inicio,
                                                        @Param("fin") String fin);
}
