package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;
import com.infinitesoft.pos_relational_data_service.entities.enums.ConfiguracionAppKey;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionAppService {
    List<ConfiguracionApp> findAll();

    ConfiguracionApp update(Long id, ConfiguracionApp config);

    ConfiguracionApp updateByKey(String key, String value);

    ConfiguracionApp findById(Long id);

    Optional<ConfiguracionApp> findByKey(ConfiguracionAppKey key);
}
