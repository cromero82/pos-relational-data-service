package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.EntradaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntradaInventarioRepository extends JpaRepository<EntradaInventario, Long> {

    Optional<EntradaInventario> findByEgresoId(Long egresoId);

    List<EntradaInventario> findByEgresoIdIn(Collection<Long> egresoIds);
}
