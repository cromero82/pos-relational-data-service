package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.TipoEgreso;
import java.util.List;

public interface TipoEgresoService {
    TipoEgreso create(TipoEgreso tipoEgreso);
    List<TipoEgreso> findAll();
    TipoEgreso findById(Long id);
    TipoEgreso update(Long id, TipoEgreso tipoEgreso);
    boolean delete(Long id);
}
