package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Ticket;
import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.dto.TicketDto;
import com.infinitesoft.pos_relational_data_service.dto.TicketReciboClienteRequest;
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository;
import com.infinitesoft.pos_relational_data_service.repositories.SesionRepository;
import com.infinitesoft.pos_relational_data_service.services.*;
import com.infinitesoft.pos_relational_data_service.security.service.AuthValidationService;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.util.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Collections;
import java.math.BigInteger;
import java.sql.Timestamp;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private AuthValidationService authValidationService;

    @Autowired
    private ClientService clientService;

    @Autowired
    @Lazy
    private ReciboService reciboService;

    @Autowired
    @Lazy
    private TicketReciboService ticketReciboService;

    @Value("${app.id-usuario-anonimo:1}")
    private Long idUsuarioAnonimo;

    @Override
    public Ticket create(Ticket ticket) {
        if (ticket.getSessionId() != null) {
            List<Ticket> existingTickets = findBySessionId(ticket.getSessionId());
            long maxOrden = 0L;
            if (!existingTickets.isEmpty()) {
                maxOrden = existingTickets.stream()
                        .mapToLong(Ticket::getOrden)
                        .max()
                        .orElse(0L);
            }
            ticket.setOrden(maxOrden + 1);
        } else if (ticket.getOrden() == null) {
            ticket.setOrden(1L);
        }
        // nombre can be null; sessionId can reference existing Sesion (nullable allowed by DB)
        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket findById(Long id) {
        if (id == null) return null;
        Optional<Ticket> opt = ticketRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<Ticket> findBySessionId(Long sessionId) {
        if (sessionId == null) return List.of();
        return ticketRepository.findBySessionIdOrderByOrdenAsc(sessionId);
    }

    @Override
    public List<TicketDto> findBySessionIdDto(Long sessionId, HttpServletRequest request) {
        if (sessionId == null) return List.of();

        String token = TokenUtils.extractToken(request);
        List<AuthUserDto> allUsers = authValidationService.getUsuarios(token);
        Map<UUID, AuthUserDto> userMap = allUsers.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(AuthUserDto::getId, u -> u, (a, b) -> a));

        AuthUserDto currentUser = authValidationService.validateToken(token);
        UUID currentUserId = null;
        if (currentUser != null && currentUser.getCorreoElectronico() != null) {
            currentUserId = authValidationService.fetchUserIdByEmail(currentUser.getCorreoElectronico());
        }

        List<Object[]> otherResults = ticketRepository.findTicketsWithNonAnonClienteByNoSessionId(sessionId, idUsuarioAnonimo);
        List<Object[]> sessionResults = ticketRepository.findTicketsWithNonAnonClienteBySessionId(sessionId, idUsuarioAnonimo);
        
        List<Object[]> results = new java.util.ArrayList<>();
        results.addAll(otherResults);
        results.addAll(sessionResults);

        Optional<Sesion> sesionOpt = sesionRepository.findById(sessionId);
        UUID sessionUserId = sesionOpt.map(Sesion::getUserId).orElse(null);

        final UUID finalCurrentUserId = currentUserId;
        final UUID finalSessionUserId = sessionUserId;

        return results.stream().map(row -> {
            // Mapping native query result:
            // [0:id, 1:sesion_id, 2:nombre, 3:orden, 4:fecha_creacion, 5:cliente_id]
            Long ticketId = ((Number) row[0]).longValue();
            Long sId = row[1] != null ? ((Number) row[1]).longValue() : null;
            String nombre = (String) row[2];
            Long orden = row[3] != null ? ((Number) row[3]).longValue() : 1L;
            
            LocalDateTime fechaCreacion = null;
            if (row[4] != null) {
                if (row[4] instanceof Timestamp) {
                    fechaCreacion = ((Timestamp) row[4]).toLocalDateTime();
                } else if (row[4] instanceof LocalDateTime) {
                    fechaCreacion = (LocalDateTime) row[4];
                }
            }

            Long clienteId = row[5] != null ? ((Number) row[5]).longValue() : null;

            TicketDto dto = TicketDto.builder()
                    .id(ticketId)
                    .sessionId(sId)
                    .nombre(nombre)
                    .orden(orden)
                    .fechaCreacion(fechaCreacion)
                    .perteneceUsuarioActual(finalCurrentUserId != null && finalCurrentUserId.equals(finalSessionUserId))
                    .build();

            // Si es de otra sesión, necesitamos cargar su propio userId para atendidoPor y perteneceUsuarioActual
            UUID effectiveSessionUserId = finalSessionUserId;
            if (sId != null && !sId.equals(sessionId)) {
                Optional<Sesion> otherSesionOpt = sesionRepository.findById(sId);
                effectiveSessionUserId = otherSesionOpt.map(Sesion::getUserId).orElse(null);
                dto.setPerteneceUsuarioActual(finalCurrentUserId != null && finalCurrentUserId.equals(effectiveSessionUserId));
            }

            // Si clienteId no es nulo, significa que NO es anónimo (debido al JOIN condicional en SQL)
            if (clienteId != null) {
                dto.setCliente(clientService.findById(clienteId));

                // Atendido por - solo se carga si el cliente NO es anónimo
                if (effectiveSessionUserId != null && userMap.containsKey(effectiveSessionUserId)) {
                    AuthUserDto atendidoUser = userMap.get(effectiveSessionUserId);
                    dto.setAtendidoPor(TicketDto.AtendidoPorDto.builder()
                            .id(atendidoUser.getId())
                            .nombre(atendidoUser.getNombre())
                            .build());
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Ticket update(Long id, Ticket ticket) {
        if (id == null) return null;
        Optional<Ticket> existingOpt = ticketRepository.findById(id);
        if (existingOpt.isEmpty()) return null;
        Ticket existing = existingOpt.get();
        // Update mutable fields only
        existing.setNombre(ticket.getNombre());
        existing.setSessionId(ticket.getSessionId());
        existing.setOrden(ticket.getOrden());
        // fechaCreacion is updatable=false, will be preserved
        return ticketRepository.save(existing);
    }

    @Override
    public Ticket updateCliente(TicketReciboClienteRequest request) {
        if (request == null || request.getTicketId() == null || request.getClienteId() == null) return null;

        Long ticketId = request.getTicketId();
        Long clienteId = request.getClienteId();

        // 1. Obtener el ticket para asegurar que existe
        Ticket ticket = findById(ticketId);
        if (ticket == null) return null;

        // 1b. Un cliente no anónimo solo puede estar en un ticket de la sesión.
        if (ticket.getSessionId() != null
                && idUsuarioAnonimo != null
                && !clienteId.equals(idUsuarioAnonimo)) {
            List<Object[]> enSesion = ticketRepository.findTicketsWithNonAnonClienteBySessionId(
                    ticket.getSessionId(), idUsuarioAnonimo);
            for (Object[] row : enSesion) {
                if (row == null || row.length < 6 || row[0] == null || row[5] == null) {
                    continue;
                }
                Long otherTicketId = ((Number) row[0]).longValue();
                Long otherClienteId = ((Number) row[5]).longValue();
                if (!otherTicketId.equals(ticketId) && otherClienteId.equals(clienteId)) {
                    throw new IllegalArgumentException(
                            "Ya existe un ticket asignado a este cliente");
                }
            }
        }

        // 2. Obtener el vínculo ticket_recibo para obtener el recibo_id
        com.infinitesoft.pos_relational_data_service.entities.TicketRecibo tr = ticketReciboService.findFirstByTicketId(ticketId);
        if (tr != null && tr.getReciboId() != null) {
            // 3. Obtener el recibo y actualizar el cliente_id
            com.infinitesoft.pos_relational_data_service.entities.Recibo recibo = reciboService.findById(tr.getReciboId());
            if (recibo != null) {
                recibo.setClienteId(clienteId);
                reciboService.saveAndFlush(recibo);
            }
        }

        return ticket;
    }

    @Override
    public List<Ticket> updateAll(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) return List.of();
        // For simplicity and to reuse the logic of update(id, ticket), we can iterate.
        // However, if performance is a concern for large lists, saveAll is better.
        // But update(id, ticket) ensures only specific fields are updated and fechaCreacion is preserved.
        // Since we are receiving full Ticket objects in the request as per example, 
        // we should probably fetch and update each to be safe.
        
        return tickets.stream()
                .map(t -> update(t.getId(), t))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!ticketRepository.existsById(id)) return false;
        ticketRepository.deleteById(id);
        return true;
    }
}
