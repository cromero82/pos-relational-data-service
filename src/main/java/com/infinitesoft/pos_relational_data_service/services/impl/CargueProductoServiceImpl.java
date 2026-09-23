package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infinitesoft.pos_relational_data_service.entities.BitacoraUsuario;
import com.infinitesoft.pos_relational_data_service.entities.CargueProducto;
import com.infinitesoft.pos_relational_data_service.entities.CargueProductoConflicto;
import com.infinitesoft.pos_relational_data_service.entities.Evento;
import com.infinitesoft.pos_relational_data_service.entities.TipoConflicto;
import com.infinitesoft.pos_relational_data_service.exception.CargueProductoException;
import com.infinitesoft.pos_relational_data_service.repositories.CargueProductoRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.CargueProductoConflictoService;
import com.infinitesoft.pos_relational_data_service.services.CargueProductoService;
import com.infinitesoft.pos_relational_data_service.services.EventoService;
import com.infinitesoft.pos_relational_data_service.services.MigrationResult;
import com.infinitesoft.pos_relational_data_service.services.MigrationService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CargueProductoServiceImpl.class);

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
            cargueProducto.setFechaCreacion(DateUtils.obtenerFechaSistema());
        }
        return repository.save(cargueProducto);
    }

    @Override
    public CargueProducto processCargue(MultipartFile file, String nombreCargue) {
        String filename = file != null ? file.getOriginalFilename() : "null";
        long fileSize = file != null ? file.getSize() : 0;
        log.info("[CARGUE] Iniciando proceso | nombre='{}' | archivo='{}' | tamaño={}bytes", nombreCargue, filename, fileSize);

        // 1. Create initial cargue record
        CargueProducto cargue = new CargueProducto();
        cargue.setNombre(nombreCargue);
        cargue.setFechaCreacion(DateUtils.obtenerFechaSistema());
        cargue.setTotalMigrados(0);
        cargue.setTotalConflictos(0);
        cargue.setTotalConflictosResultos(0);

        cargue = repository.save(cargue);
        log.info("[CARGUE] Registro inicial creado | cargueId={} | nombre='{}'", cargue.getId(), nombreCargue);

        // 2. Execute migration logic using existing service
        MigrationResult result = migrationService.importarLite(file, nombreCargue);

        if (result.isFatalError()) {
            String detalle = result.getMessages() != null && !result.getMessages().isEmpty()
                    ? result.getMessages().get(0)
                    : "Error desconocido al procesar el archivo";
            log.error("[CARGUE] Error fatal durante la migración | cargueId={} | detalle='{}'", cargue.getId(), detalle);
            repository.delete(cargue);
            throw new CargueProductoException(
                    "El archivo no pudo ser procesado. Verifique que el archivo no esté dañado y sea de formato válido (.xlsx, .xls o .csv). Detalle: " + detalle);
        }

        log.info("[CARGUE] Migración completada | cargueId={} | creados={} | conflictos={} | errores={} | omitidos={}",
                cargue.getId(),
                result.getCreated(),
                result.getConflictos() != null ? result.getConflictos().size() : 0,
                result.getErrors(),
                result.getSkipped());

        // 3. Update cargue record with results
        cargue.setTotalMigrados(result.getCreated());
        cargue.setTotalConflictos(result.getConflictos() != null ? result.getConflictos().size() : 0);

        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            cargue.setMensajesError(String.join("\n", result.getMessages()));
            log.warn("[CARGUE] cargueId={} | {} mensaje(s) de error registrados", cargue.getId(), result.getMessages().size());
        }

        cargue = repository.save(cargue);

        // 4. Process conflicts and save them to CargueProductoConflicto
        if (result.getConflictos() != null && !result.getConflictos().isEmpty()) {
            log.info("[CARGUE] Procesando {} conflicto(s) | cargueId={}", result.getConflictos().size(), cargue.getId());
            for (MigrationResult.Conflict c : result.getConflictos()) {
                CargueProductoConflicto conflicto = new CargueProductoConflicto();
                conflicto.setCargueProducto(cargue);
                conflicto.setNombreProducto(c.getNombre());
                conflicto.setResuelto(false);

                if ("Nombre y codigo de barras iguales".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.IGUAL_NOMBRE_Y_CODIGO_BARRAS);
                } else if ("2 productos con nombres iguales".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.DOS_PRODUCTOS_NOMBRES_IGUALES);
                } else if ("No tiene precio".equalsIgnoreCase(c.getReferencia())) {
                    conflicto.setTipoConflicto(TipoConflicto.NO_TIENE_PRECIO);
                } else {
                    conflicto.setTipoConflicto(TipoConflicto.DOS_PRODUCTOS_NOMBRES_IGUALES);
                }

                log.debug("[CARGUE] Conflicto | tipo={} | producto='{}' | referencia='{}'",
                        conflicto.getTipoConflicto(), c.getNombre(), c.getReferencia());

                try {
                    String jsonConflict = objectMapper.writeValueAsString(c.getConflicto());
                    conflicto.setDatosConflicto(jsonConflict);
                } catch (Exception e) {
                    log.warn("[CARGUE] No se pudo serializar datos del conflicto para producto='{}': {}", c.getNombre(), e.getMessage());
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
                log.info("[CARGUE] Bitácora registrada | cargueId={} | userId={}", cargue.getId(), userId);
            } else {
                log.warn("[CARGUE] Evento 'IMPORT_PROD' no encontrado, bitácora no registrada | cargueId={}", cargue.getId());
            }
        } catch (Exception e) {
            log.error("[CARGUE] Error al registrar bitácora | cargueId={}", cargue.getId(), e);
        }

        log.info("[CARGUE] Proceso finalizado | cargueId={} | nombre='{}' | totalMigrados={} | totalConflictos={}",
                cargue.getId(), nombreCargue, cargue.getTotalMigrados(), cargue.getTotalConflictos());
        return cargue;
    }
}
