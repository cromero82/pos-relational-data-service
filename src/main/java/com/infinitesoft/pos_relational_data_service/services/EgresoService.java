package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface EgresoService {
    Egreso create(Egreso egreso);
    List<Egreso> findAll();
    Page<Egreso> search(String descripcion, Long tipoEgresoId, Long proveedorId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    Page<Egreso> searchDescripciones(String descripcion, Pageable pageable);
    Egreso findById(Long id);
    List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Egreso> findByProveedorId(Long proveedorId);
    List<Egreso> findByTipoEgresoId(Long tipoEgresoId);
    Egreso update(Long id, Egreso egreso);
    boolean delete(Long id);
}
