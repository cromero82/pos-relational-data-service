package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDto;
import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboMetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ReciboServiceImpl implements ReciboService {

    @Autowired
    private ReciboRepository reciboRepository;

    @Autowired
    private ReciboMetodoPagoRepository reciboMetodoPagoRepository;

    @Autowired
    @Lazy
    private HistorialReciboService historialReciboService;

    @Autowired
    private HistorialReciboDetalleService historialReciboDetalleService;

    @Autowired
    private ReciboDetalleService reciboDetalleService;

    @Autowired
    private TicketReciboRepository ticketReciboRepository;

    @Autowired
    private EdicionReciboService edicionReciboService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ProductRepository productRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Persiste la lista de métodos de pago en recibo_metodo_pago.
     * Borra los anteriores y los reinserta.
     */
    private void persistMetodoPagoIds(Long reciboId, List<Long> ids) {
        reciboMetodoPagoRepository.deleteByReciboId(reciboId);
        if (ids != null && !ids.isEmpty()) {
            for (Long mpId : ids) {
                reciboMetodoPagoRepository.save(ReciboMetodoPago.builder()
                        .reciboId(reciboId)
                        .metodoPagoId(mpId)
                        .build());
            }
        }
    }

    /** Devuelve el primer id de la lista o null si está vacía. */
    private Long firstOrNull(List<Long> ids) {
        return (ids != null && !ids.isEmpty()) ? ids.get(0) : null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Recibo create(ReciboDto dto) {
        Long firstMetodoPagoId = firstOrNull(dto.getMetodoPagoIds());

        Recibo recibo = Recibo.builder()
                .clienteId(dto.getClienteId())
                .estadoId(dto.getEstadoId())
                .metodoPagoId(firstMetodoPagoId)
                .sesionId(dto.getSesionId())
                .total(dto.getTotal())
                .montoRecibido(dto.getMontoRecibido())
                .reciboPadreId(dto.getReciboIdPadre())
                .build();

        // Default estado to PENDIENTE_PAGO when not provided
        if (recibo.getEstadoId() == null) {
            recibo.setEstadoId(ReciboEstado.PENDIENTE_PAGO.getId());
        }

        Recibo saved = reciboRepository.save(recibo);
        persistMetodoPagoIds(saved.getId(), dto.getMetodoPagoIds());
        return saved;
    }

    @Override
    public Recibo saveAndFlush(Recibo recibo) {
        return reciboRepository.saveAndFlush(recibo);
    }

    @Override
    public void saveMetodoPagoIds(Long reciboId, List<Long> metodoPagoIds) {
        persistMetodoPagoIds(reciboId, metodoPagoIds);
        // También actualizar la columna de compatibilidad metodo_pago_id
        Long first = firstOrNull(metodoPagoIds);
        reciboRepository.findById(reciboId).ifPresent(r -> {
            r.setMetodoPagoId(first);
            reciboRepository.save(r);
        });
    }

    @Override
    public List<Recibo> findAll() {
        return reciboRepository.findAll();
    }

    @Override
    public Recibo findById(Long id) {
        if (id == null) return null;
        Optional<Recibo> opt = reciboRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    @Transactional
    public Recibo update(Long id, ReciboDto dto) {
        if (id == null) return null;
        Optional<Recibo> existingOpt = reciboRepository.findById(id);
        if (existingOpt.isEmpty()) return null;

        Recibo existing = existingOpt.get();

        Long firstMetodoPagoId = firstOrNull(dto.getMetodoPagoIds());

        // Update mutable fields, keep id and fechaCreacion
        existing.setClienteId(dto.getClienteId());
        existing.setEstadoId(dto.getEstadoId());
        existing.setMetodoPagoId(firstMetodoPagoId);
        // Preserve existing sesionId if not provided in the update payload
        if (dto.getSesionId() != null) {
            existing.setSesionId(dto.getSesionId());
        }
        existing.setTotal(dto.getTotal());
        existing.setMontoRecibido(dto.getMontoRecibido());
        existing.setReciboPadreId(dto.getReciboIdPadre());

        // Determine target estado
        ReciboEstado targetEstado = ReciboEstado.fromId(existing.getEstadoId());
        if (targetEstado == ReciboEstado.PAGADO || targetEstado == ReciboEstado.ANULADO) {

            // Recuperar lista actual de métodos de pago del recibo
            List<Long> currentMetodoPagoIds = dto.getMetodoPagoIds() != null && !dto.getMetodoPagoIds().isEmpty()
                    ? dto.getMetodoPagoIds()
                    : reciboMetodoPagoRepository.findMetodoPagoIdsByReciboId(id);

            // 1) Copy Recibo to HistorialRecibo (using the just-updated fields)
            HistorialRecibo hist = HistorialRecibo.builder()
                    .clienteId(existing.getClienteId())
                    .estadoId(existing.getEstadoId())
                    .metodoPagoId(existing.getMetodoPagoId())
                    .sesionId(existing.getSesionId())
                    .total(existing.getTotal())
                    .montoRecibido(existing.getMontoRecibido())
                    .build();
            hist.setMetodoPagoIds(currentMetodoPagoIds); // transient → HistorialReciboServiceImpl lo persiste
            HistorialRecibo savedHist = historialReciboService.create(hist);

            // 2) Find the link to the ticket
            Optional<TicketRecibo> ticketReciboOpt = ticketReciboRepository.findFirstByReciboId(id);

            // 3) Copy all ReciboDetalle rows into HistorialReciboDetalle linked to savedHist.id
            List<ReciboDetalle> detalles = reciboDetalleService.findEntityListByReciboId(existing.getId());
            for (ReciboDetalle d : detalles) {
                HistorialReciboDetalle hd = HistorialReciboDetalle.builder()
                        .reciboId(savedHist.getId())
                        .productoId(d.getProductoId())
                        .cantidad(d.getCantidad())
                        .subtotal(d.getSubtotal())
                        .fechaCreacion(d.getFechaCreacion())
                        .usuarioCreacion(d.getUsuarioCreacion())
                        .build();
                historialReciboDetalleService.create(hd);

                // Increment total_ventas and update fecha_ultima_venta in product
                if (targetEstado == ReciboEstado.PAGADO && d.getProductoId() != null) {
                    productRepository.incrementarVentas(d.getProductoId(), d.getCantidad());
                    productRepository.actualizarFechaUltimaVenta(d.getProductoId(), LocalDate.now());
                }
            }

            // 4) Delete all items from recibo_detalle
            reciboDetalleService.deleteByReciboId(existing.getId());

            // 5) Delete junction entries del recibo
            reciboMetodoPagoRepository.deleteByReciboId(existing.getId());

            // 6) Update EdicionRecibo if it exists
            Optional<EdicionRecibo> edicionReciboOpt = edicionReciboService.findByReciboId(id);
            if (edicionReciboOpt.isPresent()) {
                EdicionRecibo edicionRecibo = edicionReciboOpt.get();
                edicionRecibo.setHistorialReciboId(savedHist.getId());
                edicionRecibo.setReciboId(null);
                edicionReciboService.update(edicionRecibo.getId(), edicionRecibo);

                // Delete the ticket only if it came from an edition
                if (ticketReciboOpt.isPresent()) {
                    Long ticketId = ticketReciboOpt.get().getTicketId();
                    ticketReciboRepository.delete(ticketReciboOpt.get()); // Delete child first
                    ticketService.delete(ticketId); // Then delete parent
                }

                // Finally delete the recibo itself
                reciboRepository.deleteById(existing.getId());
            } else {
                // If not from an edition, still delete the link if it exists
                if (ticketReciboOpt.isPresent()) {
                    ticketReciboRepository.delete(ticketReciboOpt.get());
                }
            }

            // Return null to signal resource removal to controller
            return null;
        }

        // Actualizar junction table para estados no finales
        persistMetodoPagoIds(existing.getId(), dto.getMetodoPagoIds());

        return reciboRepository.save(existing);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!reciboRepository.existsById(id)) return false;

        // 1) Delete dependent EdicionRecibo if exists
        edicionReciboService.deleteByReciboId(id);

        // 2) Delete all items from recibo_detalle
        reciboDetalleService.deleteByReciboId(id);

        // 3) Delete links in ticket_recibo table
        ticketReciboRepository.deleteByReciboId(id);

        // 4) Delete junction entries
        reciboMetodoPagoRepository.deleteByReciboId(id);

        // 5) Finally delete the recibo itself
        reciboRepository.deleteById(id);
        return true;
    }
}
