package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    boolean existsByHistorialReciboIdAndTipoMovimientoId(Long historialReciboId, Long tipoMovimientoId);

    boolean existsByEntradaInventarioIdAndTipoMovimientoId(Long entradaInventarioId, Long tipoMovimientoId);
}
