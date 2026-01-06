package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infinitesoft.pos_relational_data_service.entities.BitacoraUsuario;
import com.infinitesoft.pos_relational_data_service.entities.CargueProducto;
import com.infinitesoft.pos_relational_data_service.entities.CargueProductoConflicto;
import com.infinitesoft.pos_relational_data_service.entities.Evento;
import com.infinitesoft.pos_relational_data_service.entities.TipoConflicto;
import com.infinitesoft.pos_relational_data_service.repositories.CargueProductoRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.CargueProductoConflictoService;
import com.infinitesoft.pos_relational_data_service.services.CargueProductoService;
import com.infinitesoft.pos_relational_data_service.services.EventoService;
import com.infinitesoft.pos_relational_data_service.services.MigrationResult;
import com.infinitesoft.pos_relational_data_service.services.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CargueProductoServiceImpl implements CargueProductoService {

    @Autowired
    private CargueProductoRepository repository;

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private CargueProductoConflictoService conflictoService;

    @Autowired
    private BitacoraUsuarioService bitacoraUsuarioService;

    @Autowired
    private EventoService eventoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public List<CargueProducto> getAll() {
        return repository.findAll();
    }

    @Override
    public CargueProducto create(CargueProducto cargueProducto) {
        if (cargueProducto.getFechaCreacion() == null) {
            cargueProducto.setFechaCreacion(LocalDateTime.now());
        }
        return repository.save(cargueProducto);
    }

    @Override
    public CargueProducto processCargue(MultipartFile file, String nombreCargue) {
        // 1. Create initial cargue record
        CargueProducto cargue = new CargueProducto();
        cargue.setNombre(nombreCargue);
        cargue.setFechaCreacion(LocalDateTime.now());
        cargue.setTotalMigrados(0);
        cargue.setTotalConflictos(0);
        cargue.setTotalConflictosResultos(0);
        
        cargue = repository.save(cargue);

        // 2. Execute migration logic using existing service
        // The eventName is now the cargue name
        MigrationResult result = migrationService.importarLite(file, nombreCargue);

        // 3. Update cargue record with results
        cargue.setTotalMigrados(result.getCreated());
        cargue.setTotalConflictos(result.getConflictos() != null ? result.getConflictos().size() : 0);
        
        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            cargue.setMensajesError(String.join("\n", result.getMessages()));
        }

        cargue = repository.save(cargue);

        // 4. Process conflicts and save them to CargueProductoConflicto
        if (result.getConflictos() != null) {
            for (MigrationResult.Conflict c : result.getConflictos()) {
                CargueProductoConflicto conflicto = new CargueProductoConflicto();
                conflicto.setCargueProducto(cargue);
                conflicto.setNombreProducto(c.getNombre());
                conflicto.setResuelto(false);

                // Determine conflict type
                if ("Nombre y codigo de barras iguales".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.IGUAL_NOMBRE_Y_CODIGO_BARRAS);
                } else if ("2 productos con nombres iguales".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.DOS_PRODUCTOS_NOMBRES_IGUALES);
                } else if ("No tiene precio".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.NO_TIENE_PRECIO);
                } else {
                    // Default or fallback if needed
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

        // 5. Register event in bitacora
        try {
            UUID userId = SecurityContextHelper.getUserId();
            Optional<Evento> eventoOpt = eventoService.findBySigla("IMPORT_PROD");

            if (eventoOpt.isPresent()) {
                BitacoraUsuario bitacora = new BitacoraUsuario();
                bitacora.setUserId(userId);
                bitacora.setEventoId(eventoOpt.get().getId());
                bitacora.setValorDespues(objectMapper.writeValueAsString(cargue));
                bitacoraUsuarioService.save(bitacora);
            } else {
                // Handle case where event is not found, maybe log a warning
            }
        } catch (Exception e) {
            // Log error during bitacora creation
            e.printStackTrace();
        }

        return cargue;
    }
}
