package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboDetalleRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class HistorialReciboServiceImpl implements HistorialReciboService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private HistorialReciboRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HistorialReciboDetalleRepository historialReciboDetalleRepository;

    @Autowired
    private HistorialReciboDetalleService historialReciboDetalleService;

    @Autowired
    @Lazy
    private ReciboService reciboService;

    @Autowired
    private ReciboDetalleService reciboDetalleService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketReciboService ticketReciboService;

    @Autowired
    private EdicionReciboService edicionReciboService;

    @Autowired
    private EdicionReciboDetalleService edicionReciboDetalleService;

    @Override
    public HistorialRecibo create(HistorialRecibo historialRecibo) {
        return repository.save(historialRecibo);
    }

    @Override
    @Transactional
    public HistorialRecibo createQuick(HistorialRecibo historialRecibo) {
        if (historialRecibo.getEstadoId() == null) {
            historialRecibo.setEstadoId(ReciboEstado.PAGADO.getId());
        }
        // 1) Save the HistorialRecibo first
        HistorialRecibo saved = repository.save(historialRecibo);

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
        historialReciboDetalleRepository.save(detalle);

        return saved;
    }

    @Override
    public List<HistorialRecibo> findAll() {
        return repository.findAll();
    }

    @Override
    public HistorialRecibo findById(Long id) {
        if (id == null) return null;
        Optional<HistorialRecibo> opt = repository.findById(id);
        return opt.orElse(null);
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
        existing.setMetodoPagoId(historialRecibo.getMetodoPagoId());
        if (historialRecibo.getSesionId() != null) {
            existing.setSesionId(historialRecibo.getSesionId());
        }
        existing.setTotal(historialRecibo.getTotal());
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
        boolean hasFecha = fecha != null && !fecha.isBlank();
        boolean hasEstado = estadoId != null;

        if (hasFecha && hasEstado) {
            try {
                LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                return repository.findByFechaCreacionBetweenAndEstadoId(startOfDay, endOfDay, estadoId, pageable);
            } catch (Exception e) {
                return Page.empty(pageable);
            }
        } else if (hasFecha) {
            try {
                LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                return repository.findByFechaCreacionBetween(startOfDay, endOfDay, pageable);
            } catch (Exception e) {
                return Page.empty(pageable);
            }
        } else if (hasEstado) {
            return repository.findByEstadoId(estadoId, pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    @Override
    @Transactional
    public void moveToEdition(Long historialReciboId, Long sesionId) {
        HistorialRecibo historial = findById(historialReciboId);
        if (historial == null) {
            // Or throw an exception
            return;
        }

        // 1. Move HistorialRecibo to Recibo
        Recibo nuevoRecibo = Recibo.builder()
                .clienteId(historial.getClienteId())
                .estadoId(ReciboEstado.EDICION.getId()) // Or whatever the default is
                .metodoPagoId(historial.getMetodoPagoId())
                .sesionId(sesionId)
                .total(historial.getTotal())
                .build();
        Recibo reciboGuardado = reciboService.create(nuevoRecibo);

        // 2. Copy HistorialRecibo to EdicionRecibo
        EdicionRecibo edicionRecibo = EdicionRecibo.builder()
                .reciboId(reciboGuardado.getId())
                .historialReciboId(historial.getId())
                .clienteId(historial.getClienteId())
                .estadoId(historial.getEstadoId())
                .metodoPagoId(historial.getMetodoPagoId())
                .sesionId(historial.getSesionId())
                .total(historial.getTotal())
                .build();
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
        LocalDateTime now = LocalDateTime.now();
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
        Ticket ticketGuardado = ticketService.create(ticket);

        // 5. Create TicketRecibo
        TicketRecibo ticketRecibo = TicketRecibo.builder()
                .ticketId(ticketGuardado.getId())
                .reciboId(reciboGuardado.getId())
                .build();
        ticketReciboService.create(ticketRecibo);

        // 6. Delete original HistorialRecibo and its details
        historialReciboDetalleService.deleteByReciboId(historialReciboId);
        repository.delete(historial);
    }
}
