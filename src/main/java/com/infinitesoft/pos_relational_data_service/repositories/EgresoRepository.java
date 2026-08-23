package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaEgreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EgresoRepository extends JpaRepository<Egreso, Long> {
    List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Egreso> findByProveedorId(Long proveedorId);

    Optional<Egreso> findByFromMovimientoOrigenFondosId(Long fromMovimientoOrigenFondosId);

    @Query("SELECT e FROM Egreso e LEFT JOIN e.tipoEgreso t LEFT JOIN e.proveedor p LEFT JOIN p.tipoEgreso pt "
            + "WHERE t.id = :tipoEgresoId OR (t IS NULL AND pt.id = :tipoEgresoId)")
    List<Egreso> findByTipoEgresoId(@Param("tipoEgresoId") Long tipoEgresoId);

    @Query("SELECT e FROM Egreso e LEFT JOIN e.proveedor p LEFT JOIN e.tipoEgreso t LEFT JOIN p.tipoEgreso pt WHERE "
            + "(:descripcion IS NULL OR LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%'))) AND "
            + "(:tipoEgresoId IS NULL OR t.id = :tipoEgresoId OR (t IS NULL AND pt.id = :tipoEgresoId)) AND "
            + "(:naturaleza IS NULL OR e.naturaleza = :naturaleza) AND "
            + "(:proveedorId IS NULL OR p.id = :proveedorId) AND "
            + "(CAST(:fechaInicio AS date) IS NULL OR e.fecha >= :fechaInicio) AND "
            + "(CAST(:fechaFin AS date) IS NULL OR e.fecha <= :fechaFin)")
    Page<Egreso> search(@Param("descripcion") String descripcion,
                        @Param("tipoEgresoId") Long tipoEgresoId,
                        @Param("naturaleza") NaturalezaEgreso naturaleza,
                        @Param("proveedorId") Long proveedorId,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin,
                        Pageable pageable);

    @Query("SELECT e FROM Egreso e LEFT JOIN e.proveedor p LEFT JOIN e.tipoEgreso te LEFT JOIN p.tipoEgreso t WHERE "
            + "(:texto IS NULL OR :texto = '' OR "
            + "LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')) OR "
            + "(p IS NOT NULL AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) OR "
            + "(te IS NOT NULL AND LOWER(te.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) OR "
            + "(t IS NOT NULL AND LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))))")
    Page<Egreso> searchDescripciones(@Param("texto") String texto, Pageable pageable);

    @Query("SELECT e.metodoPagoId, SUM(e.valor) FROM Egreso e "
            + "WHERE e.metodoPagoId IS NOT NULL "
            + "AND (e.fechaCreacion >= :start AND e.fechaCreacion <= :end "
            + "     OR (e.fecha >= :fechaDesde AND e.fecha <= :fechaHasta)) "
            + "GROUP BY e.metodoPagoId")
    List<Object[]> findResumenEgresosPorMetodoPago(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("fechaDesde") LocalDate fechaDesde,
                                                   @Param("fechaHasta") LocalDate fechaHasta);
}
