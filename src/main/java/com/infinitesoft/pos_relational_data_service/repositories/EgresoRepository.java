package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EgresoRepository extends JpaRepository<Egreso, Long> {
    List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Egreso> findByProveedorId(Long proveedorId);
    List<Egreso> findByTipoEgresoId(Long tipoEgresoId);
}
