package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.TicketReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.ClientService;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import com.infinitesoft.pos_relational_data_service.services.SesionService;
import com.infinitesoft.pos_relational_data_service.services.TicketReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TicketReciboServiceImpl implements TicketReciboService {

    @Autowired
    private TicketReciboRepository repository;

    @Autowired
    private ReciboService reciboService;

    @Autowired
    private ClientService clientService;

    @Autowired
    @Lazy
    private SesionService sesionService;

    @Override
    public TicketRecibo create(TicketRecibo tr) {
        return repository.save(tr);
    }

    @Override
    public TicketRecibo createAndFlush(Long ticketId, Long reciboId) {
        TicketRecibo ticketRecibo = TicketRecibo.builder()
                .ticketId(ticketId)
                .reciboId(reciboId)
                .build();
        return repository.saveAndFlush(ticketRecibo);
    }

    @Override
    public List<TicketRecibo> findAll() {
        return repository.findAll();
    }

    @Override
    public TicketRecibo findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public TicketRecibo update(Long id, TicketRecibo tr) {
        if (id == null) return null;
        Optional<TicketRecibo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        TicketRecibo existing = existingOpt.get();
        existing.setTicketId(tr.getTicketId());
        existing.setReciboId(tr.getReciboId());
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public TicketRecibo getOrCreateByTicketId(Long ticketId, Long sessionId) {
        if (ticketId == null || sessionId == null) return null;

        // Update session with the latest ticket ID
        Sesion sesion = sesionService.findById(sessionId);
        if (sesion != null) {
            sesion.setUltimoTicketId(ticketId);
            sesionService.update(sessionId, sesion);
        }

        Optional<TicketRecibo> existing = repository.findFirstByTicketId(ticketId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Resolve default client 'ANONIMO' via ClientService
        Client anonimo = clientService.findByNombre("ANONIMO");
        if (anonimo == null) {
            anonimo = Client.builder()
                    .nombre("ANONIMO")
                    .build();
            anonimo = clientService.create(anonimo);
        }
        Long clienteId = anonimo.getId();
        // Create a minimal Recibo using defaults and the ANONIMO client
        Recibo nuevoRecibo = Recibo.builder()
                .clienteId(clienteId)
                .estadoId(ReciboEstado.PENDIENTE_PAGO.getId())
                .metodoPagoId(null)
                .sesionId(sessionId)
                .total(BigDecimal.ZERO)
                .build();
        Recibo savedRecibo = reciboService.create(nuevoRecibo);

        TicketRecibo enlace = TicketRecibo.builder()
                .ticketId(ticketId)
                .reciboId(savedRecibo.getId())
                .build();
        return repository.save(enlace);
    }

    @Override
    public TicketRecibo findFirstByTicketId(Long ticketId) {
        if (ticketId == null) return null;
        return repository.findFirstByTicketId(ticketId).orElse(null);
    }

    @Override
    public List<TicketRecibo> findByTicketId(Long ticketId) {
        if (ticketId == null) return List.of();
        return repository.findAllByTicketId(ticketId);
    }

    @Override
    public long deleteByReciboId(Long reciboId) {
        if (reciboId == null) return 0L;
        return repository.deleteByReciboId(reciboId);
    }
}
