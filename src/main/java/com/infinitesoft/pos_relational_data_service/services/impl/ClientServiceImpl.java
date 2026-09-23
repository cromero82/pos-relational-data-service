package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infinitesoft.pos_relational_data_service.entities.BitacoraUsuario;
import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.entities.Evento;
import com.infinitesoft.pos_relational_data_service.repositories.ClientRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.ClientService;
import com.infinitesoft.pos_relational_data_service.services.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

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
    public Client create(Client client) {
        // If you want to enforce default at app level:
        // if (client.getNombre() == null || client.getNombre().isBlank()) client.setNombre("anonimo");
        Client savedClient = clientRepository.save(client);
        registrarBitacora(null, savedClient, "REG_CLIENTE");
        return savedClient;
    }

    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client findById(Long id) {
        if (id == null) return null;
        return clientRepository.findById(id).orElse(null);
    }

    @Override
    public Client findByNombre(String nombre) {
        if (nombre == null) return null;
        return clientRepository.findFirstByNombreIgnoreCase(nombre).orElse(null);
    }

    @Override
    @Transactional
    public Client update(Long id, Client client) {
        if (id == null) return null;
        
        Optional<Client> existingOpt = clientRepository.findById(id);
        if (existingOpt.isEmpty()) return null;
        Client existing = existingOpt.get();

        // En algunos casos, el 'client' que llega por el controlador puede ser el mismo objeto
        // que 'existing' debido al binding de Spring (DomainClassConverter).
        // Si ya son iguales, el estado 'antes' capturado aquí ya tendría los valores nuevos.
        
        Client antes = Client.builder()
                .id(existing.getId())
                .nombre(existing.getNombre())
                .telefono(existing.getTelefono())
                .documento(existing.getDocumento())
                .build();

        // Actualizamos los campos
        existing.setNombre(client.getNombre());
        existing.setTelefono(client.getTelefono());
        existing.setDocumento(client.getDocumento());

        // Forzamos el guardado para asegurar que los cambios persistan antes de la bitácora
        Client updatedClient = clientRepository.saveAndFlush(existing);

        // Si detectamos que los valores siguen siendo iguales, es probable que 'antes' se capturó tarde.
        // Pero con la lógica actual de builder() debería ser suficiente si son instancias distintas.
        registrarBitacora(antes, updatedClient, "MOD_CLIENTE");

        return updatedClient;
    }

    private void registrarBitacora(Client antes, Client despues, String siglaEvento) {
        try {
            UUID userId = SecurityContextHelper.getUserId();
            Optional<Evento> eventoOpt = eventoService.findBySigla(siglaEvento);

            if (eventoOpt.isPresent()) {
                BitacoraUsuario bitacora = new BitacoraUsuario();
                bitacora.setUserId(userId);
                bitacora.setEventoId(eventoOpt.get().getId());
                if (antes != null) {
                    bitacora.setValorAntes(objectMapper.writeValueAsString(antes));
                }
                if (despues != null) {
                    bitacora.setValorDespues(objectMapper.writeValueAsString(despues));
                    if (despues.getId() != null) {
                        bitacora.setReferenciaId(despues.getId().intValue());
                    }
                }
                bitacoraUsuarioService.save(bitacora);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!clientRepository.existsById(id)) return false;
        clientRepository.deleteById(id);
        return true;
    }
}
