package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import java.time.LocalDate;
import java.util.List;

public interface EgresoService {
    Egreso create(Egreso egreso);
    List<Egreso> findAll();
    Egreso findById(Long id);
    List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Egreso> findByProveedorId(Long proveedorId);
    List<Egreso> findByTipoEgresoId(Long tipoEgresoId);
    Egreso update(Long id, Egreso egreso);
    boolean delete(Long id);
}
