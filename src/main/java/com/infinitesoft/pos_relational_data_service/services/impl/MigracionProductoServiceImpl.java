package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.entities.MigracionProducto;
import com.infinitesoft.pos_relational_data_service.entities.MigracionProductoConflicto;
import com.infinitesoft.pos_relational_data_service.entities.TipoConflicto;
import com.infinitesoft.pos_relational_data_service.repositories.MigracionProductoRepository;
import com.infinitesoft.pos_relational_data_service.services.MigracionProductoConflictoService;
import com.infinitesoft.pos_relational_data_service.services.MigracionProductoService;
import com.infinitesoft.pos_relational_data_service.services.MigrationResult;
import com.infinitesoft.pos_relational_data_service.services.MigrationService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MigracionProductoServiceImpl implements MigracionProductoService {

    @Autowired
    private MigracionProductoRepository repository;

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private MigracionProductoConflictoService conflictoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<MigracionProducto> getAll() {
        return repository.findAll();
    }

    @Override
    public MigracionProducto create(MigracionProducto migracionProducto) {
        if (migracionProducto.getFechaCreacion() == null) {
            migracionProducto.setFechaCreacion(DateUtils.obtenerFechaSistema());
        }
        return repository.save(migracionProducto);
    }

    @Override
    public MigracionProducto processMigration(MultipartFile file, String nombreMigracion) {
        // 1. Create initial migration record
        MigracionProducto migracion = new MigracionProducto();
        migracion.setNombre(nombreMigracion);
        migracion.setFechaCreacion(DateUtils.obtenerFechaSistema());
        migracion.setTotalMigrados(0);
        migracion.setTotalConflictos(0);
        migracion.setTotalConflictosResultos(0);
        
        migracion = repository.save(migracion);

        // 2. Execute migration logic using existing service
        // The eventName is now the migration name
        MigrationResult result = migrationService.importarLite(file, nombreMigracion);

        // 3. Update migration record with results
        migracion.setTotalMigrados(result.getCreated());
        migracion.setTotalConflictos(result.getConflictos() != null ? result.getConflictos().size() : 0);
        
        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            migracion.setMensajesError(String.join("\n", result.getMessages()));
        }

        migracion = repository.save(migracion);

        // 4. Process conflicts and save them to MigracionProductoConflicto
        if (result.getConflictos() != null) {
            for (MigrationResult.Conflict c : result.getConflictos()) {
                MigracionProductoConflicto conflicto = new MigracionProductoConflicto();
                conflicto.setMigracionProducto(migracion);
                conflicto.setNombreProducto(c.getNombre());
                conflicto.setResuelto(false);

                // Determine conflict type
                if ("Nombre y codigo de barras iguales".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.IGUAL_NOMBRE_Y_CODIGO_BARRAS);
                } else if ("2 productos con nombres iguales".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.DOS_PRODUCTOS_NOMBRES_IGUALES);
                } else {
                    // Default or fallback if needed, though enum only has 2 values currently
                    // Assuming DOS_PRODUCTOS_NOMBRES_IGUALES as default or handle error
                    conflicto.setTipoConflicto(TipoConflicto.DOS_PRODUCTOS_NOMBRES_IGUALES);
                }

                try {
                    String jsonConflict = objectMapper.writeValueAsString(c.getConflicto());
                    conflicto.setDatosConflicto(jsonConflict);
                } catch (Exception e) {
                    conflicto.setDatosConflicto("{}");
                }

                conflictoService.create(conflicto);
            }
        }

        return migracion;
    }
}
