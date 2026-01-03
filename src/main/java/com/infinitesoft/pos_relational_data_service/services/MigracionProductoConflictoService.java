package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.MigracionProductoConflicto;
import java.util.List;

public interface MigracionProductoConflictoService {
    List<MigracionProductoConflicto> getAll(Integer migracionProductoId);
    MigracionProductoConflicto create(MigracionProductoConflicto migracionProductoConflicto);
}
