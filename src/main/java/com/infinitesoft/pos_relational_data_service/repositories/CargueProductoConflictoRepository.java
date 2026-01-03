package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.CargueProductoConflicto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargueProductoConflictoRepository extends JpaRepository<CargueProductoConflicto, Integer> {
    List<CargueProductoConflicto> findByCargueProductoId(Integer cargueProductoId);
    List<CargueProductoConflicto> findByCargueProductoIdAndResuelto(Integer cargueProductoId, Boolean resuelto);
}
