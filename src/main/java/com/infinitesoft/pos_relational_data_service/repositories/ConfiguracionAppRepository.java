package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionAppRepository extends JpaRepository<ConfiguracionApp, Long> {
    Optional<ConfiguracionApp> findByKey(String key);
}
