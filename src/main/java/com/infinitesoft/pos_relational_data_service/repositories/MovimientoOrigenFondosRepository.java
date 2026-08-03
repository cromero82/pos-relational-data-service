package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.MovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoMovimientoOrigenFondos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoOrigenFondosRepository extends JpaRepository<MovimientoOrigenFondos, Long> {

    @Query("SELECT COALESCE(SUM(m.impacto), 0) FROM MovimientoOrigenFondos m WHERE m.origenFondosId = :cuentaId")
    BigDecimal sumImpactoByCuentaId(@Param("cuentaId") Integer cuentaId);

    List<MovimientoOrigenFondos> findByOrigenFondosIdOrderByFechaCreacionDescIdDesc(Integer origenFondosId);

    List<MovimientoOrigenFondos> findByOrigenTipoAndIdReferenciaOrderByIdAsc(String origenTipo, Long idReferencia);

    Optional<MovimientoOrigenFondos> findFirstByOrigenTipoOrderByIdAsc(String origenTipo);

    boolean existsByOrigenTipo(String origenTipo);

    List<MovimientoOrigenFondos> findByGrupoTrasladoIdOrderByIdAsc(String grupoTrasladoId);

    @Query("SELECT MAX(m.id) FROM MovimientoOrigenFondos m")
    Optional<Long> findMaxId();

    @Query("SELECT MIN(m.fechaCreacion) FROM MovimientoOrigenFondos m WHERE m.id > :afterId")
    Optional<LocalDateTime> findMinFechaCreacionByIdGreaterThan(@Param("afterId") Long afterId);

    @Query("SELECT MAX(m.fechaCreacion) FROM MovimientoOrigenFondos m WHERE m.id > :afterId")
    Optional<LocalDateTime> findMaxFechaCreacionByIdGreaterThan(@Param("afterId") Long afterId);

    @Query("SELECT MAX(m.id) FROM MovimientoOrigenFondos m "
            + "WHERE m.fechaCreacion >= :start AND m.fechaCreacion <= :end")
    Optional<Long> findMaxIdByFechaCreacionBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Otros movimientos del ledger por medio (excluye egresos y ventas, que tienen columna propia en cierre).
     */
    @Query("SELECT m.metodoPagoId, COALESCE(SUM(m.impacto), 0) "
            + "FROM MovimientoOrigenFondos m "
            + "WHERE m.fechaCreacion >= :start AND m.fechaCreacion <= :end "
            + "AND m.metodoPagoId IS NOT NULL "
            + "AND m.tipoMovimiento NOT IN :excluidos "
            + "GROUP BY m.metodoPagoId")
    List<Object[]> findResumenMovimientosPorMetodoPago(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excluidos") List<TipoMovimientoOrigenFondos> excluidos);

    /**
     * Movimientos del turno abierto: id estrictamente mayor al watermark del último corte.
     */
    @Query("SELECT m.metodoPagoId, COALESCE(SUM(m.impacto), 0) "
            + "FROM MovimientoOrigenFondos m "
            + "WHERE m.id > :afterId "
            + "AND m.fechaCreacion <= :end "
            + "AND m.metodoPagoId IS NOT NULL "
            + "AND m.tipoMovimiento NOT IN :excluidos "
            + "GROUP BY m.metodoPagoId")
    List<Object[]> findResumenMovimientosPorMetodoPagoAfterId(
            @Param("afterId") Long afterId,
            @Param("end") LocalDateTime end,
            @Param("excluidos") List<TipoMovimientoOrigenFondos> excluidos);
}
