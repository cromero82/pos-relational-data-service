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
import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
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

    @Override
    public SesionDto create(SesionDto sesionDto, HttpServletRequest request) {
        String token = TokenUtils.extractToken(request);
        Sesion sesion = toEntity(sesionDto);
        UUID resolvedUserId = resolveUserIdFromToken(token);
        if (resolvedUserId == null) return null;
        sesion.setUserId(resolvedUserId);
        // Set defaults on create
        sesion.setEsActivo(Boolean.TRUE);
        if (sesion.getFechaInicio() == null) {
            sesion.setFechaInicio(DateUtils.obtenerFechaSistema());
        }
        Sesion saved = repository.save(sesion);
        return toDto(saved);
    }

    @Override
    public List<SesionDto> findAll(HttpServletRequest request) {
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (userId == null) return List.of();
        return repository.findByUserIdAndEsActivoTrue(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SesionDto> findAllByUserAllStates(HttpServletRequest request) {
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (userId == null) return List.of();
        return repository.findByUserIdOrderByFechaInicioDesc(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public SesionDto findById(Long id, HttpServletRequest request) {
        if (id == null ) return null;
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public AuthUserDto findUserInfoBySesionId(Long sesionId, HttpServletRequest request) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SesionServiceImpl.class);
        logger.info("[SesionService] Iniciando búsqueda de usuario para sesión id={}", sesionId);

        if (sesionId == null) return null;
        Optional<Sesion> sesionOpt = repository.findById(sesionId);
        if (sesionOpt.isEmpty()) {
            logger.warn("[SesionService] No se encontró la sesión con id={}", sesionId);
            return null;
        }

        UUID userId = sesionOpt.get().getUserId();
        if (userId == null) {
            logger.warn("[SesionService] La sesión id={} no tiene un userId asociado", sesionId);
            return null;
        }

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
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (id == null || userId == null) return null;
        Optional<Sesion> existingOpt = repository.findByIdAndUserIdAndEsActivoTrue(id, userId);
        if (existingOpt.isEmpty()) return null;
        Sesion existing = existingOpt.get();
        existing.setCookie(sesionDto.getCookie());
        existing.setUltimoTicketId(sesionDto.getUltimoTicketId());
        Sesion saved = repository.save(existing);
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
        UUID userId = resolveUserIdFromToken(TokenUtils.extractToken(request));
        if (id == null || userId == null) return false;
        Optional<Sesion> existing = repository.findByIdAndUserIdAndEsActivoTrue(id, userId);
        if (existing.isEmpty()) return false;
        // Cascade delete: ticket_recibo -> ticket -> soft delete session
        List<Ticket> tickets = ticketService.findBySessionId(id);
        if (tickets != null) {
            for (Ticket t : tickets) {
                if (t == null || t.getId() == null) continue;
                List<TicketRecibo> ticketsRecibo = ticketReciboService.findByTicketId(t.getId());
                if (ticketsRecibo != null) {
                    for (TicketRecibo tr : ticketsRecibo) {
                        if (tr != null && tr.getId() != null) {
                            // Delete recibo_detalle and recibo, then the link
                            Long reciboId = tr.getReciboId();
                            if (reciboId != null) {
                                try {
                                    reciboDetalleService.deleteByReciboId(reciboId);
                                } catch (Exception e) {
                                    org.slf4j.LoggerFactory.getLogger(SesionServiceImpl.class)
                                            .error("[SesionDelete] Error deleting recibo_detalle for reciboId={}: {}", reciboId, e.toString(), e);
                                }
                                try {
                                    reciboService.delete(reciboId);
                                } catch (Exception e) {
                                    org.slf4j.LoggerFactory.getLogger(SesionServiceImpl.class)
                                            .error("[SesionDelete] Error deleting recibo reciboId={}: {}", reciboId, e.toString(), e);
                                }
                            }
                            ticketReciboService.delete(tr.getId());
                        }
                    }
                }
                ticketService.delete(t.getId());
            }
        }
        // Soft delete session at the end
        Sesion s = existing.get();
        s.setEsActivo(Boolean.FALSE);
        s.setFechaFin(DateUtils.obtenerFechaSistema());
        repository.save(s);
        return true;
    }

    private UUID resolveUserIdFromToken(String token) {
        if (token == null || token.isBlank()) return null;
        AuthUserDto user = authValidationService.validateToken(token);
        if (user != null && user.getCorreoElectronico() != null && !user.getCorreoElectronico().isBlank()) {
            return authValidationService.fetchUserIdByEmail(user.getCorreoElectronico());
        }
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
