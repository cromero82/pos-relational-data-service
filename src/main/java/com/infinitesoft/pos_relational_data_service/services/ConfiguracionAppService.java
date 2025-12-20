package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;

import java.util.List;

public interface ConfiguracionAppService {
    List<ConfiguracionApp> findAll();

    ConfiguracionApp update(Long id, ConfiguracionApp config);

    ConfiguracionApp findById(Long id);
}
