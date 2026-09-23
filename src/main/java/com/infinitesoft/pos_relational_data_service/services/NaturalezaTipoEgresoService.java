package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.NaturalezaTipoEgreso;

import java.util.List;

public interface NaturalezaTipoEgresoService {
    NaturalezaTipoEgreso create(NaturalezaTipoEgreso entity);

    List<NaturalezaTipoEgreso> findAll();

    List<NaturalezaTipoEgreso> findActivas();

    NaturalezaTipoEgreso findById(Long id);

    NaturalezaTipoEgreso update(Long id, NaturalezaTipoEgreso entity);

    boolean delete(Long id);
}
