package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.MigracionProducto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MigracionProductoService {
    List<MigracionProducto> getAll();
    MigracionProducto create(MigracionProducto migracionProducto);
    MigracionProducto processMigration(MultipartFile file, String nombreMigracion);
}
