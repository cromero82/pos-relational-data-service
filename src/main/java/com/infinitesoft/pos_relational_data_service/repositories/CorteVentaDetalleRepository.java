package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CorteVentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CorteVentaDetalleRepository extends JpaRepository<CorteVentaDetalle, Long> {
    List<CorteVentaDetalle> findByCorteVentaIdOrderByOrdenAscIdAsc(Long corteVentaId);

    /**
     * Ventas POS de cortes vigentes cuyo registro cae en el rango
     * (mismo criterio de día que el dashboard de Ingresos).
     */
    @Query("SELECT COALESCE(SUM(d.totalVentasSistema), 0) FROM CorteVentaDetalle d, CorteVenta c "
            + "WHERE d.corteVentaId = c.id AND c.estado <> 'eliminado' "
            + "AND c.fechaCreacion >= :start AND c.fechaCreacion <= :end")
    BigDecimal sumVentasSistemaCortesVigentes(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
