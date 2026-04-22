package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.entities.Ticket;
import com.infinitesoft.pos_relational_data_service.repositories.SesionRepository;
import com.infinitesoft.pos_relational_data_service.services.SesionService;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.service.AuthValidationService;
import com.infinitesoft.pos_relational_data_service.security.util.TokenUtils;
import com.infinitesoft.pos_relational_data_service.dto.SesionDto;
import com.infinitesoft.pos_relational_data_service.services.TicketService;
import com.infinitesoft.pos_relational_data_service.services.TicketReciboService;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleService;
import com.infinitesoft.pos_relational_data_service.services.ClientService;
import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SesionServiceImpl implements SesionService {

    @Autowired
    private SesionRepository repository;

    @Autowired
    private AuthValidationService authValidationService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketReciboService ticketReciboService;

    @Autowired
    private ReciboService reciboService;

    @Autowired
    private ReciboDetalleService reciboDetalleService;

    @Autowired
    private ClientService clientService;

    @Value("${app.id-usuario-anonimo:1}")
    private Long idUsuarioAnonimo;

    @Override
    public SesionDto create(SesionDto sesionDto, HttpServletRequest request) {
        log.info("Iniciando servicio SesionServiceImpl - Método: create - DTO: {}", sesionDto);
        String token = TokenUtils.extractToken(request);
        Sesion sesion = toEntity(sesionDto);
        UUID resolvedUserId = resolveUserIdFromToken(token);
        if (resolvedUserId == null) {
            log.error("No se pudo resolver el userId desde el token para la creación de sesión");
            return null;
        }
        sesion.setUserId(resolvedUserId);
        // Set defaults on create
        sesion.setEsActivo(Boolean.TRUE);
        if (sesion.getFechaInicio() == null) {
            sesion.setFechaInicio(DateUtils.obtenerFechaSistema());
        }
        Sesion saved = repository.save(sesion);
        log.info("Sesión creada exitosamente - ID: {}, UserId: {}", saved.getId(), saved.getUserId());
        return toDto(saved);
    }

    @Override
    public List<SesionDto> findAll(HttpServletRequest request) {
        log.info("Iniciando servicio SesionServiceImpl - Método: findAll");
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (userId == null) {
            log.warn("UserId no resuelto para findAll");
            return List.of();
        }
        log.info("Buscando sesiones activas para UserId: {}", userId);
        return repository.findByUserIdAndEsActivoTrue(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SesionDto> findAllByUserAllStates(HttpServletRequest request) {
        log.info("Iniciando servicio SesionServiceImpl - Método: findAllByUserAllStates");
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (userId == null) {
            log.warn("UserId no resuelto para findAllByUserAllStates");
            return List.of();
        }
        log.info("Buscando todas las sesiones para UserId: {}", userId);
        return repository.findByUserIdOrderByFechaInicioDesc(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public SesionDto findById(Long id, HttpServletRequest request) {
        log.info("Iniciando servicio SesionServiceImpl - Método: findById - ID: {}", id);
        if (id == null ) return null;
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public AuthUserDto findUserInfoBySesionId(Long sesionId, HttpServletRequest request) {
        log.info("Iniciando servicio SesionServiceImpl - Método: findUserInfoBySesionId - SesionID: {}", sesionId);

        if (sesionId == null) return null;
        Optional<Sesion> sesionOpt = repository.findById(sesionId);
        if (sesionOpt.isEmpty()) {
            log.warn("No se encontró la sesión con id={}", sesionId);
            return null;
        }

        UUID userId = sesionOpt.get().getUserId();
        if (userId == null) {
            log.warn("La sesión id={} no tiene un userId asociado", sesionId);
            return null;
        }

        log.info("Buscando información de AuthUser para UserId: {}", userId);
        return authValidationService.fetchUserInfoById(userId);
    }



    // Legacy overload (internal usage where userId context is not provided)
    @Override
    public Sesion findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public SesionDto update(Long id, SesionDto sesionDto, HttpServletRequest request) {
        log.info("Iniciando servicio SesionServiceImpl - Método: update - ID: {} - DTO: {}", id, sesionDto);
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (id == null || userId == null) {
            log.warn("ID o UserId nulo en update de sesión");
            return null;
        }
        Optional<Sesion> existingOpt = repository.findByIdAndUserIdAndEsActivoTrue(id, userId);
        if (existingOpt.isEmpty()) {
            log.warn("Sesión activa no encontrada para ID: {} y UserId: {}", id, userId);
            return null;
        }
        Sesion existing = existingOpt.get();
        existing.setCookie(sesionDto.getCookie());
        existing.setUltimoTicketId(sesionDto.getUltimoTicketId());
        Sesion saved = repository.save(existing);
        log.info("Sesión actualizada - ID: {}", saved.getId());
        return toDto(saved);
    }

    // Legacy overload (internal usage where userId context is not provided)
    @Override
    public Sesion update(Long id, Sesion sesion) {
        if (id == null || sesion == null) return null;
        Optional<Sesion> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        Sesion existing = existingOpt.get();
        existing.setCookie(sesion.getCookie());
        existing.setUltimoTicketId(sesion.getUltimoTicketId());
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id, HttpServletRequest request) {
        log.info("Iniciando servicio SesionServiceImpl - Método: delete (Soft Delete) - ID: {}", id);
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (id == null || userId == null) {
            log.warn("ID o UserId nulo en delete de sesión");
            return false;
        }
        Optional<Sesion> existing = repository.findByIdAndUserIdAndEsActivoTrue(id, userId);
        if (existing.isEmpty()) {
            log.warn("Sesión activa no encontrada para eliminación - ID: {} y UserId: {}", id, userId);
            return false;
        }
        
        // Cascade delete: ticket_recibo -> ticket -> soft delete session
        log.info("Iniciando proceso de cascada para eliminación de sesión ID: {}", id);
        List<Ticket> tickets = ticketService.findBySessionId(id);
        if (tickets != null) {
            log.info("Se encontraron {} tickets asociados a la sesión", tickets.size());
            for (Ticket t : tickets) {
                if (t == null || t.getId() == null) continue;
                List<TicketRecibo> ticketsRecibo = ticketReciboService.findByTicketId(t.getId());
                boolean allowedToDelete = false;
                if (ticketsRecibo != null && !ticketsRecibo.isEmpty()) {
                    for (TicketRecibo tr : ticketsRecibo) {
                        if (tr != null && tr.getReciboId() != null) {
                            Recibo recibo = reciboService.findById(tr.getReciboId());
                            if (recibo != null && recibo.getClienteId() != null && recibo.getClienteId().equals(idUsuarioAnonimo)) {
                                allowedToDelete = true;
                                break;
                            }
                        }
                    }
                }

                if (allowedToDelete) {
                    log.info("Eliminando Ticket ID: {} y sus recibos asociados (cliente anónimo)", t.getId());
                    if (ticketsRecibo != null) {
                        for (TicketRecibo tr : ticketsRecibo) {
                            if (tr != null && tr.getId() != null) {
                                // Delete recibo_detalle and recibo, then the link
                                Long reciboId = tr.getReciboId();
                                if (reciboId != null) {
                                    try {
                                        log.info("Eliminando detalles de recibo para reciboId={}", reciboId);
                                        reciboDetalleService.deleteByReciboId(reciboId);
                                        log.info("Eliminando reciboId={}", reciboId);
                                        reciboService.delete(reciboId);
                                    } catch (Exception e) {
                                        log.error("[SesionDelete] Error deleting recibo o detalles para reciboId={}: {}", reciboId, e.toString());
                                    }
                                }
                                ticketReciboService.delete(tr.getId());
                            }
                        }
                    }
                    ticketService.delete(t.getId());
                } else {
                    log.info("Ticket ID: {} no permitido para eliminación (no es cliente anónimo o no tiene recibos)", t.getId());
                }
            }
        }
        
        // Soft delete session at the end
        Sesion s = existing.get();
        s.setEsActivo(Boolean.FALSE);
        s.setFechaFin(DateUtils.obtenerFechaSistema());
        repository.save(s);
        log.info("Sesión ID: {} marcada como inactiva (Soft Delete completado)", id);
        return true;
    }

    private UUID resolveUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Token vacío o nulo al intentar resolver UserId");
            return null;
        }
        AuthUserDto user = authValidationService.validateToken(token);
        if (user != null && user.getCorreoElectronico() != null && !user.getCorreoElectronico().isBlank()) {
            UUID userId = authValidationService.fetchUserIdByEmail(user.getCorreoElectronico());
            log.info("UserId resuelto para correo {}: {}", user.getCorreoElectronico(), userId);
            return userId;
        }
        log.warn("No se pudo obtener información de usuario desde el token");
        return null;
    }

    // Mapping helpers moved into service per requirement
    private SesionDto toDto(Sesion s) {
        if (s == null) return null;
        return SesionDto.builder()
                .id(s.getId())
                .cookie(s.getCookie())
                .ultimoTicketId(s.getUltimoTicketId())
                .fechaInicio(s.getFechaInicio())
                .fechaFin(s.getFechaFin())
                .build();
    }

    private Sesion toEntity(SesionDto d) {
        if (d == null) return null;
        return Sesion.builder()
                .id(d.getId())
                .cookie(d.getCookie())
                .ultimoTicketId(d.getUltimoTicketId())
                .fechaInicio(d.getFechaInicio())
                .fechaFin(d.getFechaFin())
                .build();
    }
}