package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    boolean existsByHistorialReciboIdAndTipoMovimientoId(Long historialReciboId, Long tipoMovimientoId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MovimientoInventario m SET m.historialReciboId = NULL WHERE m.historialReciboId = :historialReciboId")
    int detachHistorialRecibo(@Param("historialReciboId") Long historialReciboId);

    boolean existsByEntradaInventarioIdAndTipoMovimientoId(Long entradaInventarioId, Long tipoMovimientoId);

    boolean existsByReciboIdAndTipoMovimientoId(Long reciboId, Long tipoMovimientoId);

    java.util.Optional<MovimientoInventario> findFirstByReciboIdAndTipoMovimientoId(
            Long reciboId, Long tipoMovimientoId);
}
