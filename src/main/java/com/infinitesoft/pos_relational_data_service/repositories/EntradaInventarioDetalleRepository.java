package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.EntradaInventarioDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntradaInventarioDetalleRepository extends JpaRepository<EntradaInventarioDetalle, Long> {

    List<EntradaInventarioDetalle> findByEntradaIdOrderByIdAsc(Long entradaId);
}
