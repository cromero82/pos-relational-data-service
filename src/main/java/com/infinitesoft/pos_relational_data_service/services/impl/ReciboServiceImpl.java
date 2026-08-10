package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboPagoResponseDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboUpdateResult;
import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.ClientRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboElectronicoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketReciboRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReciboServiceImpl implements ReciboService {

    @Autowired
    private ReciboRepository reciboRepository;

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

    @Autowired
    private HistorialReciboElectronicoRepository historialReciboElectronicoRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private DocumentoVentaService documentoVentaService;

    @Autowired
    private MovimientoInventarioService movimientoInventarioService;

    @Autowired
    private ClientRepository clientRepository;

    @Value("${app.id-usuario-anonimo:1}")
    private Long idUsuarioAnonimo;

    @Override
    public Recibo create(ReciboDto dto) {
        Recibo recibo = Recibo.builder()
                .clienteId(dto.getClienteId())
                .estadoId(dto.getEstadoId())
                .metodoPagoId(dto.getMetodoPagoId())
                .sesionId(dto.getSesionId())
                .total(dto.getTotal())
                .montoRecibido(dto.getMontoRecibido())
                .reciboPadreId(dto.getReciboIdPadre())
                .build();

        // Default estado to PENDIENTE_PAGO when not provided
        if (recibo.getEstadoId() == null) {
            recibo.setEstadoId(ReciboEstado.PENDIENTE_PAGO.getId());
        }
        return reciboRepository.save(recibo);
    }

    @Override
    public Recibo saveAndFlush(Recibo recibo) {
        return reciboRepository.saveAndFlush(recibo);
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
    public ReciboUpdateResult update(Long id, ReciboDto dto) {
        if (id == null) return ReciboUpdateResult.fromRecibo(null);
        Optional<Recibo> existingOpt = reciboRepository.findById(id);
        if (existingOpt.isEmpty()) return ReciboUpdateResult.fromRecibo(null);

        Recibo existing = existingOpt.get();
        // Update mutable fields, keep id and fechaCreacion
        existing.setClienteId(resolverClienteIdActualizacion(existing.getClienteId(), dto.getClienteId()));
        existing.setEstadoId(dto.getEstadoId());
        existing.setMetodoPagoId(dto.getMetodoPagoId());
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
            // 1) Copy Recibo to HistorialRecibo (using the just-updated fields)
            HistorialRecibo hist = HistorialRecibo.builder()
                    .clienteId(existing.getClienteId())
                    .estadoId(existing.getEstadoId())
                    .metodoPagoId(existing.getMetodoPagoId())
                    .sesionId(existing.getSesionId())
                    .total(existing.getTotal())
                    .montoRecibido(existing.getMontoRecibido())
                    .build();
            HistorialRecibo savedHist = historialReciboService.create(hist);
            if (targetEstado == ReciboEstado.PAGADO) {
                registrarPendienteConfirmacionElectronica(savedHist, existing);
            }

            DocumentoVenta documentoVenta = null;
            if (targetEstado == ReciboEstado.PAGADO) {
                try {
                    documentoVenta = documentoVentaService.crearDesdeHistorialRecibo(
                            savedHist, SecurityContextHelper.getUserId());
                    savedHist.setDocumentoVentaId(documentoVenta.getId());
                    savedHist.setDocumentoVentaConsecutivo(documentoVenta.getConsecutivo());
                } catch (Exception e) {
                    // Sprint 0 SQL pendiente: el pago no debe fallar por documento_venta.
                }
            }

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

            if (targetEstado == ReciboEstado.PAGADO) {
                try {
                    List<HistorialReciboDetalle> detallesHistorial =
                            historialReciboDetalleService.findEntityListByReciboId(savedHist.getId());
                    movimientoInventarioService.registrarVentaPos(
                            savedHist,
                            documentoVenta != null ? documentoVenta.getId() : null,
                            detallesHistorial,
                            SecurityContextHelper.getUserId());
                } catch (Exception e) {
                    // Sprint 3 SQL pendiente: el pago no debe fallar por kardex.
                }
            }

            // 4) Delete all items from recibo_detalle
            reciboDetalleService.deleteByReciboId(existing.getId());

            // 5) Update EdicionRecibo if it exists
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

            // Return pago response to controller
            if (targetEstado == ReciboEstado.PAGADO && documentoVenta != null) {
                ReciboPagoResponseDto pago = ReciboPagoResponseDto.builder()
                        .pagado(true)
                        .historialReciboId(savedHist.getId())
                        .documentoVentaId(documentoVenta.getId())
                        .documentoVentaConsecutivo(documentoVenta.getConsecutivo())
                        .total(savedHist.getTotal())
                        .fechaCreacion(savedHist.getFechaCreacion())
                        .metodoPagoId(savedHist.getMetodoPagoId())
                        .clienteId(savedHist.getClienteId())
                        .sesionId(savedHist.getSesionId())
                        .build();
                return ReciboUpdateResult.fromPago(pago);
            }

            return ReciboUpdateResult.fromPago(ReciboPagoResponseDto.builder()
                    .pagado(targetEstado == ReciboEstado.PAGADO)
                    .historialReciboId(savedHist.getId())
                    .total(savedHist.getTotal())
                    .fechaCreacion(savedHist.getFechaCreacion())
                    .metodoPagoId(savedHist.getMetodoPagoId())
                    .clienteId(savedHist.getClienteId())
                    .sesionId(savedHist.getSesionId())
                    .build());
        }

        return ReciboUpdateResult.fromRecibo(reciboRepository.save(existing));
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

        // 4) Finally delete the recibo itself
        reciboRepository.deleteById(id);
        return true;
    }

    /**
     * Ventas QR (sigla QR / Bancolombia): deja pendiente de confirmación por email bancario.
     * Best-effort: no debe tumbar el pago si falla.
     */
    private void registrarPendienteConfirmacionElectronica(HistorialRecibo hist, Recibo recibo) {
        try {
            if (hist == null || hist.getId() == null || hist.getMetodoPagoId() == null) {
                return;
            }
            if (historialReciboElectronicoRepository.findByHistorialReciboId(hist.getId()).isPresent()) {
                return;
            }
            Optional<MetodoPago> mpOpt = metodoPagoRepository.findById(hist.getMetodoPagoId());
            if (mpOpt.isEmpty()) {
                return;
            }
            MetodoPago mp = mpOpt.get();
            String sigla = mp.getSigla() != null ? mp.getSigla().trim().toUpperCase() : "";
            String desc = mp.getDescripcion() != null ? mp.getDescripcion().toUpperCase() : "";
            boolean esQrElectronico = "QR".equals(sigla) || desc.contains("BANCOLOMBIA");
            if (!esQrElectronico) {
                return;
            }
            historialReciboElectronicoRepository.save(HistorialReciboElectronico.builder()
                    .historialReciboId(hist.getId())
                    .sesionId(hist.getSesionId())
                    .metodoPagoId(hist.getMetodoPagoId())
                    .montoEsperado(hist.getTotal())
                    .estado("CREADA")
                    .nombreCliente(resolverNombreClienteTicket(recibo, hist))
                    .build());
        } catch (Exception ignored) {
            // Feature no crítica
        }
    }

    private Long resolverClienteIdActualizacion(Long actual, Long dtoClienteId) {
        if (dtoClienteId == null) {
            return actual;
        }
        if (esClienteAnonimo(dtoClienteId) && !esClienteAnonimo(actual)) {
            return actual;
        }
        return dtoClienteId;
    }

    private boolean esClienteAnonimo(Long clienteId) {
        if (clienteId == null) {
            return true;
        }
        return idUsuarioAnonimo != null && clienteId.equals(idUsuarioAnonimo);
    }

    private String resolverNombreClienteTicket(Recibo recibo, HistorialRecibo hist) {
        Long clienteId = hist != null && hist.getClienteId() != null
                ? hist.getClienteId()
                : (recibo != null ? recibo.getClienteId() : null);
        if (!esClienteAnonimo(clienteId)) {
            Optional<Client> clientOpt = clientRepository.findById(clienteId);
            if (clientOpt.isPresent()) {
                String nombre = clientOpt.get().getNombre();
                if (nombre != null && !nombre.isBlank() && !esNombreAnonimo(nombre)) {
                    return nombre.trim();
                }
            }
        }
        if (recibo == null || recibo.getId() == null) {
            return null;
        }
        Optional<TicketRecibo> tr = ticketReciboRepository.findFirstByReciboId(recibo.getId());
        if (tr.isEmpty() || tr.get().getTicketId() == null) {
            return null;
        }
        Ticket ticket = ticketService.findById(tr.get().getTicketId());
        if (ticket == null || ticket.getNombre() == null || ticket.getNombre().isBlank()) {
            return null;
        }
        String nombreTicket = ticket.getNombre().trim();
        if (nombreTicket.matches("(?i)ticket\\s*\\d+")) {
            return null;
        }
        return nombreTicket;
    }

    private static boolean esNombreAnonimo(String nombre) {
        String n = nombre.trim();
        return n.equalsIgnoreCase("anonimo") || n.equalsIgnoreCase("anónimo");
    }
}
