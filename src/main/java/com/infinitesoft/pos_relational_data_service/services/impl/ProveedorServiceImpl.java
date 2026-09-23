package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioRequest;
import com.infinitesoft.pos_relational_data_service.entities.Proveedor;
import com.infinitesoft.pos_relational_data_service.repositories.ProveedorRepository;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.ProveedorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorServiceImpl.class);

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private BitacoraUsuarioService bitacoraUsuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Proveedor create(Proveedor proveedor) {
        logger.info("Iniciando servicio ProveedorService: create para proveedor: {}", proveedor.getNombre());
        if (proveedor.getDocumento() == null || proveedor.getDocumento().trim().isEmpty()) {
            proveedor.setDocumento(UUID.randomUUID().toString());
        }
        Proveedor saved = proveedorRepository.save(proveedor);
        registrarBitacora(null, saved, "REG_PROVEEDOR");
        return saved;
    }

    @Override
    public List<Proveedor> findAll() {
        logger.info("Iniciando servicio ProveedorService: findAll");
        return proveedorRepository.findAll();
    }

    @Override
    public Proveedor findById(Long id) {
        logger.info("Iniciando servicio ProveedorService: findById con id: {}", id);
        return proveedorRepository.findById(id).orElse(null);
    }

    @Override
    public Proveedor findByDocumento(String documento) {
        logger.info("Iniciando servicio ProveedorService: findByDocumento con documento: {}", documento);
        return proveedorRepository.findByDocumento(documento).orElse(null);
    }

    @Override
    public Proveedor findByNombre(String nombre) {
        logger.info("Iniciando servicio ProveedorService: findByNombre con nombre: {}", nombre);
        return proveedorRepository.findFirstByNombreIgnoreCase(nombre).orElse(null);
    }

    @Override
    @Transactional
    public Proveedor update(Long id, Proveedor proveedor) {
        logger.info("Iniciando servicio ProveedorService: update para id: {}", id);
        Optional<Proveedor> existingOpt = proveedorRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }

        Proveedor existing = existingOpt.get();
        Proveedor antes = Proveedor.builder()
                .id(existing.getId())
                .documento(existing.getDocumento())
                .nombre(existing.getNombre())
                .telefono(existing.getTelefono())
                .correo(existing.getCorreo())
                .tipoEgreso(existing.getTipoEgreso())
                .build();

        existing.setDocumento(proveedor.getDocumento());
        existing.setNombre(proveedor.getNombre());
        existing.setTelefono(proveedor.getTelefono());
        existing.setCorreo(proveedor.getCorreo());
        existing.setTipoEgreso(proveedor.getTipoEgreso());

        Proveedor updated = proveedorRepository.save(existing);
        registrarBitacora(antes, updated, "MOD_PROVEEDOR");
        return updated;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        logger.info("Iniciando servicio ProveedorService: delete para id: {}", id);
        if (!proveedorRepository.existsById(id)) {
            return false;
        }
        proveedorRepository.deleteById(id);
        return true;
    }

    private void registrarBitacora(Proveedor antes, Proveedor despues, String siglaEvento) {
        try {
            BitacoraUsuarioRequest request = new BitacoraUsuarioRequest();
            request.setEvento(siglaEvento);
            if (antes != null) {
                request.setValorAntes(objectMapper.writeValueAsString(antes));
            }
            if (despues != null) {
                request.setValorDespues(objectMapper.writeValueAsString(despues));
                if (despues.getId() != null) {
                    request.setReferenciaId(despues.getId().intValue());
                }
            }
            bitacoraUsuarioService.save(request);
        } catch (Exception e) {
            logger.error("Error al registrar bitácora para proveedor: {}", e.getMessage());
        }
    }
}
