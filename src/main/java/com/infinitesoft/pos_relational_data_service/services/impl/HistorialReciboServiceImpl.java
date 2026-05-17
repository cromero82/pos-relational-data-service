package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboMetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository;
import com.infinitesoft.pos_relational_data_service.services.*;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistorialReciboServiceImpl implements HistorialReciboService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private HistorialReciboRepository repository;

    @Autowired
    private HistorialReciboMetodoPagoRepository historialReciboMetodoPagoRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HistorialReciboDetalleService historialReciboDetalleService;

    @Autowired
    @Lazy
    private ReciboService reciboService;

    @Autowired
    private ReciboDetalleService reciboDetalleService;

    @Autowired
    @Lazy
    private TicketService ticketService;

    @Autowired
    @Lazy
    private TicketReciboService ticketReciboService;

    @Autowired
    private EdicionReciboService edicionReciboService;

    @Autowired
    private EdicionReciboDetalleService edicionReciboDetalleService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SesionService sesionService;

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Persiste la lista de métodos de pago en historial_recibo_metodo_pago.
     * Borra los anteriores y los reinserta.
     */
    private void persistMetodoPagoIds(Long historialReciboId, List<Long> ids) {
        historialReciboMetodoPagoRepository.deleteByHistorialReciboId(historialReciboId);
        if (ids != null && !ids.isEmpty()) {
            for (Long mpId : ids) {
                historialReciboMetodoPagoRepository.save(HistorialReciboMetodoPago.builder()
                        .historialReciboId(historialReciboId)
                        .metodoPagoId(mpId)
                        .build());
            }
        }
    }

    /**
     * Resuelve la lista efectiva de métodos de pago dada la entidad:
     * prioriza el campo transient metodoPagoIds; si está vacío/null cae al campo
     * de compatibilidad metodoPagoId.
     */
    private List<Long> resolveMetodoPagoIds(HistorialRecibo hr) {
        if (hr.getMetodoPagoIds() != null && !hr.getMetodoPagoIds().isEmpty()) {
            return hr.getMetodoPagoIds();
        }
        if (hr.getMetodoPagoId() != null) {
            return Collections.singletonList(hr.getMetodoPagoId());
        }
        return Collections.emptyList();
    }

    /** Puebla el campo transient metodoPagoIds en una lista de entidades. */
    private void populateMetodoPagoIds(List<HistorialRecibo> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> ids = list.stream().map(HistorialRecibo::getId).collect(Collectors.toSet());
        Map<Long, List<Long>> junctionMap = historialReciboMetodoPagoRepository
                .findByHistorialReciboIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        HistorialReciboMetodoPago::getHistorialReciboId,
                        Collectors.mapping(HistorialReciboMetodoPago::getMetodoPagoId, Collectors.toList())
                ));
        list.forEach(hr -> hr.setMetodoPagoIds(junctionMap.getOrDefault(hr.getId(), Collections.emptyList())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public HistorialRecibo create(HistorialRecibo historialRecibo) {
        HistorialRecibo saved = repository.save(historialRecibo);
        List<Long> ids = resolveMetodoPagoIds(historialRecibo);
        persistMetodoPagoIds(saved.getId(), ids);
        saved.setMetodoPagoIds(ids);
        return saved;
    }

    @Override
    @Transactional
    public HistorialRecibo createQuick(HistorialRecibo historialRecibo) {
        if (historialRecibo.getEstadoId() == null) {
            historialRecibo.setEstadoId(ReciboEstado.PAGADO.getId());
        }
        // 1) Save the HistorialRecibo first
        HistorialRecibo saved = repository.save(historialRecibo);

        // Persistir lista de métodos de pago
        List<Long> ids = resolveMetodoPagoIds(historialRecibo);
        persistMetodoPagoIds(saved.getId(), ids);
        saved.setMetodoPagoIds(ids);

        // 2) Ensure the VARIOSPROD product exists
        final String VARIOS_BARCODE = "VARIOSPROD";
        Product varios = productRepository.findByBarcode(VARIOS_BARCODE).orElse(null);
        if (varios == null) {
            varios = Product.builder()
                    .barcode(VARIOS_BARCODE)
                    .nombre("N-PRODUCTOS")
                    .precio(0.0)
                    .precioCompra(0.0)
                    .activate(1)
                    .build();
            varios = productRepository.save(varios);
        }

        // 3) Create HistorialReciboDetalle linked to the saved historial recibo
        BigDecimal subtotal = saved.getTotal() == null ? BigDecimal.ZERO : saved.getTotal();
        HistorialReciboDetalle detalle = HistorialReciboDetalle.builder()
                .reciboId(saved.getId())
                .productoId(varios.getId())
                .cantidad(1)
                .subtotal(subtotal)
                .build();
        historialReciboDetalleService.create(detalle);

        return saved;
    }

    @Override
    public List<HistorialRecibo> findAll() {
        List<HistorialRecibo> list = repository.findAll();
        populateMetodoPagoIds(list);
        return list;
    }

    @Override
    public HistorialRecibo findById(Long id) {
        if (id == null) return null;
        Optional<HistorialRecibo> opt = repository.findById(id);
        if (opt.isEmpty()) return null;
        HistorialRecibo hr = opt.get();
        hr.setMetodoPagoIds(historialReciboMetodoPagoRepository.findMetodoPagoIdsByHistorialReciboId(id));
        return hr;
    }

    @Override
    @Transactional
    public HistorialRecibo update(Long id, HistorialRecibo historialRecibo, Long sesionId) {
        if (historialRecibo.getEstadoId().equals(ReciboEstado.EDICION.getId())) {
            moveToEdition(id, sesionId);
            return null; // Or a specific object indicating the move
        }

        if (id == null) return null;
        Optional<HistorialRecibo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        HistorialRecibo existing = existingOpt.get();
        existing.setClienteId(historialRecibo.getClienteId());
        existing.setEstadoId(historialRecibo.getEstadoId());

        // Resolver lista de métodos de pago
        List<Long> newIds = resolveMetodoPagoIds(historialRecibo);
        Long firstMetodoPagoId = newIds.isEmpty() ? null : newIds.get(0);
        existing.setMetodoPagoId(firstMetodoPagoId);

        if (historialRecibo.getSesionId() != null) {
            existing.setSesionId(historialRecibo.getSesionId());
        }
        existing.setTotal(historialRecibo.getTotal());
        existing.setMontoRecibido(historialRecibo.getMontoRecibido());
        HistorialRecibo saved = repository.save(existing);

        // Actualizar junction table
        persistMetodoPagoIds(id, newIds);
        saved.setMetodoPagoIds(newIds);
        return saved;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        historialReciboMetodoPagoRepository.deleteByHistorialReciboId(id);
        repository.deleteById(id);
        return true;
    }

    @Override
    public BigDecimal getTotalByDate(String fecha) {
        if (fecha == null) return BigDecimal.ZERO;
        try {
            LocalDate date = LocalDate.parse(fecha, DATE_FMT);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            return repository.sumTotalByFechaCreacionBetween(startOfDay, endOfDay);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Page<HistorialRecibo> search(String fecha, Long estadoId, Pageable pageable) {
        return search(fecha, estadoId, 0L, pageable);
    }

    @Override
    public Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Pageable pageable) {
        boolean hasFecha = fecha != null && !fecha.isBlank();
        boolean hasEstado = estadoId != null && estadoId != 0;
        boolean hasSesion = sesionId != null && sesionId > 0;

        List<HistorialRecibo> results;
        Page<HistorialRecibo> page;

        if (hasSesion) {
            if (hasFecha && hasEstado) {
                try {
                    LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                    results = repository.findBySesionIdAndFechaCreacionBetweenAndEstadoId(sesionId, startOfDay, endOfDay, estadoId);
                } catch (Exception e) {
                    results = List.of();
                }
            } else if (hasFecha) {
                try {
                    LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                    results = repository.findBySesionIdAndFechaCreacionBetween(sesionId, startOfDay, endOfDay);
                } catch (Exception e) {
                    results = List.of();
                }
            } else if (hasEstado) {
                results = repository.findBySesionIdAndEstadoId(sesionId, estadoId);
            } else {
                results = repository.findBySesionId(sesionId);
            }
            populateMetodoPagoIds(results);
            return new PageImpl<>(results);
        }

        if (hasFecha && hasEstado) {
            try {
                LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                page = repository.findByFechaCreacionBetweenAndEstadoId(startOfDay, endOfDay, estadoId, pageable);
            } catch (Exception e) {
                return Page.empty(pageable);
            }
        } else if (hasFecha) {
            try {
                LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                page = repository.findByFechaCreacionBetween(startOfDay, endOfDay, pageable);
            } catch (Exception e) {
                return Page.empty(pageable);
            }
        } else if (hasEstado) {
            page = repository.findByEstadoId(estadoId, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        populateMetodoPagoIds(page.getContent());
        return page;
    }

    @Override
    @Transactional
    public void moveToEdition(Long historialReciboId, Long sesionId) {
        HistorialRecibo historial = findById(historialReciboId); // ya incluye metodoPagoIds
        if (historial == null) {
            return;
        }

        // Recuperar lista de métodos de pago del historial
        List<Long> metodoPagoIds = historial.getMetodoPagoIds();
        if (metodoPagoIds == null || metodoPagoIds.isEmpty()) {
            // Fallback a campo de compatibilidad
            if (historial.getMetodoPagoId() != null) {
                metodoPagoIds = Collections.singletonList(historial.getMetodoPagoId());
            } else {
                metodoPagoIds = Collections.emptyList();
            }
        }
        Long firstMetodoPagoId = metodoPagoIds.isEmpty() ? null : metodoPagoIds.get(0);

        // 1. Move HistorialRecibo to Recibo
        Recibo nuevoRecibo = Recibo.builder()
                .clienteId(historial.getClienteId())
                .estadoId(ReciboEstado.EDICION.getId())
                .metodoPagoId(firstMetodoPagoId)
                .sesionId(sesionId)
                .total(historial.getTotal())
                .montoRecibido(historial.getMontoRecibido())
                .build();
        Recibo reciboGuardado = reciboService.saveAndFlush(nuevoRecibo);
        // Persistir lista de métodos de pago para el recibo nuevo
        reciboService.saveMetodoPagoIds(reciboGuardado.getId(), metodoPagoIds);

        // 2. Copy HistorialRecibo to EdicionRecibo
        EdicionRecibo edicionRecibo = EdicionRecibo.builder()
                .reciboId(reciboGuardado.getId())
                .historialReciboId(historial.getId())
                .clienteId(historial.getClienteId())
                .estadoId(historial.getEstadoId())
                .metodoPagoId(firstMetodoPagoId)
                .sesionId(historial.getSesionId())
                .total(historial.getTotal())
                .montoRecibido(historial.getMontoRecibido())
                .build();
        edicionRecibo.setMetodoPagoIds(metodoPagoIds);
        EdicionRecibo edicionReciboGuardado = edicionReciboService.create(edicionRecibo);

        // 3. Move and Copy Details
        List<HistorialReciboDetalle> detallesHistorial = historialReciboDetalleService.findEntityListByReciboId(historialReciboId);
        for (HistorialReciboDetalle detalleHistorial : detallesHistorial) {
            // a. Move to ReciboDetalle
            ReciboDetalle nuevoDetalle = ReciboDetalle.builder()
                    .reciboId(reciboGuardado.getId())
                    .productoId(detalleHistorial.getProductoId())
                    .cantidad(detalleHistorial.getCantidad())
                    .subtotal(detalleHistorial.getSubtotal())
                    .fechaCreacion(detalleHistorial.getFechaCreacion())
                    .usuarioCreacion(detalleHistorial.getUsuarioCreacion())
                    .build();
            reciboDetalleService.create(nuevoDetalle);

            // b. Copy to EdicionReciboDetalle
            EdicionReciboDetalle edicionDetalle = EdicionReciboDetalle.builder()
                    .edicionId(edicionReciboGuardado.getId())
                    .productoId(detalleHistorial.getProductoId())
                    .cantidad(detalleHistorial.getCantidad())
                    .subtotal(detalleHistorial.getSubtotal())
                    .build();
            edicionReciboDetalleService.create(edicionDetalle);
        }

        // 4. Create Ticket
        LocalDateTime now = DateUtils.obtenerFechaSistema();
        String ticketName;
        DateTimeFormatter formatter;
        if (now.toLocalDate().equals(LocalDate.now())) {
            formatter = DateTimeFormatter.ofPattern("'historial hoy' hh:mm a", new Locale("es", "ES"));
        } else {
            formatter = DateTimeFormatter.ofPattern("'historial' dd MMM hh:mm a", new Locale("es", "ES"));
        }
        ticketName = now.format(formatter).toLowerCase();

        Ticket ticket = Ticket.builder()
                .sessionId(sesionId)
                .nombre(ticketName)
                .build();
        Ticket ticketGuardado = ticketRepository.saveAndFlush(ticket);

        // 5. Create TicketRecibo
        ticketReciboService.createAndFlush(ticketGuardado.getId(), reciboGuardado.getId());

        // 6. Update Session with the new ticket ID
        Sesion sesion = sesionService.findById(sesionId);
        if (sesion != null) {
            sesion.setUltimoTicketId(ticketGuardado.getId());
            sesionService.update(sesionId, sesion);
        }

        // 7. Delete original HistorialRecibo: junction + detalles + registro
        historialReciboMetodoPagoRepository.deleteByHistorialReciboId(historialReciboId);
        historialReciboDetalleService.deleteByReciboId(historialReciboId);
        repository.delete(historial);
    }
}
