package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;
import com.infinitesoft.pos_relational_data_service.repositories.ConfiguracionAppRepository;
import com.infinitesoft.pos_relational_data_service.services.ConfiguracionAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracionAppServiceImpl implements ConfiguracionAppService {

    @Autowired
    private ConfiguracionAppRepository repository;

    @Override
    public List<ConfiguracionApp> findAll() {
        return repository.findAll();
    }

    @Override
    public ConfiguracionApp update(Long id, ConfiguracionApp config) {
        if (id == null) return null;
        Optional<ConfiguracionApp> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        ConfiguracionApp existing = existingOpt.get();
        // Sólo se actualizan key y value
        existing.setKey(config.getKey());
        existing.setValue(config.getValue());
        return repository.save(existing);
    }

    @Override
    public ConfiguracionApp findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }
}
