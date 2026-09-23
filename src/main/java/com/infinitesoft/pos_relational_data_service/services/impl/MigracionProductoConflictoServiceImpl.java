package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.MigracionProductoConflicto;
import com.infinitesoft.pos_relational_data_service.repositories.MigracionProductoConflictoRepository;
import com.infinitesoft.pos_relational_data_service.services.MigracionProductoConflictoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MigracionProductoConflictoServiceImpl implements MigracionProductoConflictoService {

    @Autowired
    private MigracionProductoConflictoRepository repository;

    @Override
    public List<MigracionProductoConflicto> getAll(Integer migracionProductoId) {
        if (migracionProductoId != null) {
            return repository.findByMigracionProductoId(migracionProductoId);
        }
        return repository.findAll();
    }

    @Override
    public MigracionProductoConflicto create(MigracionProductoConflicto migracionProductoConflicto) {
        return repository.save(migracionProductoConflicto);
    }
}
