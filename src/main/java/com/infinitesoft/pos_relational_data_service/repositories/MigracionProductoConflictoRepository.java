package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.MigracionProductoConflicto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MigracionProductoConflictoRepository extends JpaRepository<MigracionProductoConflicto, Integer> {
    List<MigracionProductoConflicto> findByMigracionProductoId(Integer migracionProductoId);
}
