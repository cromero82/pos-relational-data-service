package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioDto;
import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioRequest;
import com.infinitesoft.pos_relational_data_service.entities.BitacoraUsuario;
import com.infinitesoft.pos_relational_data_service.entities.Evento;
import com.infinitesoft.pos_relational_data_service.repositories.BitacoraUsuarioRepository;
import com.infinitesoft.pos_relational_data_service.security.client.AuthClient;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.EventoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BitacoraUsuarioServiceImpl implements BitacoraUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(BitacoraUsuarioServiceImpl.class);
    private final BitacoraUsuarioRepository bitacoraUsuarioRepository;
    private final AuthClient authClient;
    private final EventoService eventoService;

    @Autowired
    public BitacoraUsuarioServiceImpl(BitacoraUsuarioRepository bitacoraUsuarioRepository, AuthClient authClient, EventoService eventoService) {
        this.bitacoraUsuarioRepository = bitacoraUsuarioRepository;
        this.authClient = authClient;
        this.eventoService = eventoService;
    }

    @Override
    public List<BitacoraUsuario> findAll() {
        logger.info("Iniciando servicio BitacoraUsuarioService: findAll");
        return bitacoraUsuarioRepository.findAll();
    }

    @Override
    public Optional<BitacoraUsuario> findById(Integer id) {
        logger.info("Iniciando servicio BitacoraUsuarioService: findById con id: {}", id);
        return bitacoraUsuarioRepository.findById(id);
    }

    @Override
    public BitacoraUsuario save(BitacoraUsuario bitacoraUsuario) {
        logger.info("Iniciando servicio BitacoraUsuarioService: save");
        return bitacoraUsuarioRepository.save(bitacoraUsuario);
    }

    @Override
    public BitacoraUsuario save(BitacoraUsuarioRequest request) {
        logger.info("Iniciando servicio BitacoraUsuarioService: save con request");
        BitacoraUsuario bitacoraUsuario = new BitacoraUsuario();
        bitacoraUsuario.setUserId(SecurityContextHelper.getUserId());
        bitacoraUsuario.setValorAntes(request.getValorAntes());
        bitacoraUsuario.setValorDespues(request.getValorDespues());

        if (request.getEvento() != null && !request.getEvento().isEmpty()) {
            Optional<Evento> evento = eventoService.findBySigla(request.getEvento());
            evento.ifPresent(e -> bitacoraUsuario.setEventoId(e.getId()));
        } else {
            bitacoraUsuario.setEventoId(request.getEventoId());
        }

        return bitacoraUsuarioRepository.save(bitacoraUsuario);
    }

    @Override
    public void deleteById(Integer id) {
        logger.info("Iniciando servicio BitacoraUsuarioService: deleteById con id: {}", id);
        bitacoraUsuarioRepository.deleteById(id);
    }

    @Override
    public Page<BitacoraUsuarioDto> search(UUID userId, LocalDate fecha, Pageable pageable, String token) {
        logger.info("Iniciando servicio BitacoraUsuarioService: search con userId: {} y fecha: {}", userId, fecha);
        
        // 1. Obtener la página de entidades
        Page<BitacoraUsuario> page = bitacoraUsuarioRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            if (fecha != null) {
                predicates.add(criteriaBuilder.between(root.get("fechaCreacion"), fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        // 2. Obtener todos los usuarios del servicio de autenticación
        List<AuthUserDto> usuarios = authClient.getUsuarios(token);
        Map<String, AuthUserDto> usuariosMap = usuarios.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(u -> u.getId().toString(), Function.identity(), (existing, replacement) -> existing));

        // 3. Obtener todos los eventos
        List<Evento> eventos = eventoService.findAll();
        Map<Integer, Evento> eventosMap = eventos.stream()
                .collect(Collectors.toMap(Evento::getId, Function.identity()));

        // 4. Mapear a DTO y enriquecer con datos de usuario y evento
        return page.map(entity -> {
            BitacoraUsuarioDto dto = new BitacoraUsuarioDto();
            dto.setId(entity.getId());
            dto.setUserId(entity.getUserId());
            dto.setValorAntes(entity.getValorAntes());
            dto.setValorDespues(entity.getValorDespues());
            dto.setFechaCreacion(entity.getFechaCreacion());
            
            if (entity.getUserId() != null) {
                dto.setUsuario(usuariosMap.get(entity.getUserId().toString()));
            }

            if (entity.getEventoId() != null) {
                dto.setEvento(eventosMap.get(entity.getEventoId()));
            }
            
            return dto;
        });
    }
}
