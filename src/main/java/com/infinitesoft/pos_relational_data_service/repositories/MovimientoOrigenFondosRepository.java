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

    /**
     * Entradas «por identificar» en bolsas (p.ej. Para ordenar) aún no formalizadas como egreso.
     */
    @Query("SELECT m FROM MovimientoOrigenFondos m "
            + "WHERE m.origenFondosId IN :origenFondosIds "
            + "AND m.valor = :valor "
            + "AND m.impacto > 0 "
            + "AND UPPER(TRIM(m.origenTipo)) = 'MOVIMIENTO BANCO POR IDENTIFICAR' "
            + "AND NOT EXISTS ("
            + "  SELECT 1 FROM Egreso e WHERE e.fromMovimientoOrigenFondosId = m.id"
            + ") "
            + "ORDER BY m.id DESC")
    List<MovimientoOrigenFondos> findCandidatosFormalizarEgreso(
            @Param("origenFondosIds") List<Integer> origenFondosIds,
            @Param("valor") BigDecimal valor);

    @Query("SELECT m FROM MovimientoOrigenFondos m "
            + "WHERE m.clasificacionOperativa IS NOT NULL "
            + "AND (:clasificacion IS NULL OR m.clasificacionOperativa = :clasificacion) "
            + "AND m.fecha >= :desde AND m.fecha <= :hasta "
            + "AND m.impacto > 0 "
            + "ORDER BY m.fecha DESC, m.id DESC")
    List<MovimientoOrigenFondos> findPorClasificacionOperativa(
            @Param("clasificacion") String clasificacion,
            @Param("desde") java.time.LocalDate desde,
            @Param("hasta") java.time.LocalDate hasta);

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
     * Otros movimientos del ledger por medio (excluye egresos documento y ventas del corte).
     * Excepción: {@code QR_MONTO_DISTINTO} (sobrepago/faltante QR) SÍ suma aunque el tipo sea
     * {@code SALIDA_EGRESO} / {@code ENTRADA_MANUAL} — no es egreso de proveedor.
     */
    @Query("SELECT m.metodoPagoId, COALESCE(SUM(m.impacto), 0) "
            + "FROM MovimientoOrigenFondos m "
            + "WHERE m.fechaCreacion >= :start AND m.fechaCreacion <= :end "
            + "AND m.metodoPagoId IS NOT NULL "
            + "AND (m.tipoMovimiento NOT IN :excluidos "
            + "     OR UPPER(TRIM(COALESCE(m.origenTipo, ''))) = 'QR_MONTO_DISTINTO') "
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
            + "AND (m.tipoMovimiento NOT IN :excluidos "
            + "     OR UPPER(TRIM(COALESCE(m.origenTipo, ''))) = 'QR_MONTO_DISTINTO') "
            + "GROUP BY m.metodoPagoId")
    List<Object[]> findResumenMovimientosPorMetodoPagoAfterId(
            @Param("afterId") Long afterId,
            @Param("end") LocalDateTime end,
            @Param("excluidos") List<TipoMovimientoOrigenFondos> excluidos);
}
