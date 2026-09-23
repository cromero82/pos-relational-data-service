package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.HistorialDocumentosDto;
import com.infinitesoft.pos_relational_data_service.dto.MotivoOperacionRequestDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboPagoLineaDto;
import com.infinitesoft.pos_relational_data_service.dto.RestaurarTicketResponseDto;
import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.entities.enums.DocumentoVentaEstado;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.CorteVentaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.DocumentoVentaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EdicionReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboElectronicoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.SesionRepository;
import com.infinitesoft.pos_relational_data_service.repositories.NotaAjusteDocumentoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.*;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HistorialReciboServiceImpl implements HistorialReciboService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private HistorialReciboRepository repository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private HistorialReciboPagoRepository historialReciboPagoRepository;

    @Autowired
    private HistorialReciboElectronicoRepository historialReciboElectronicoRepository;

    @Autowired
    private CorteVentaRepository corteVentaRepository;

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

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private DocumentoVentaService documentoVentaService;

    @Autowired
    private NotaAjusteService notaAjusteService;

    @Autowired
    private MovimientoInventarioService movimientoInventarioService;

    @Autowired
    private NotaAjusteDocumentoRepository notaAjusteDocumentoRepository;

    @Autowired
    private EdicionReciboRepository edicionReciboRepository;

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
        historialRecibo.setTicketRapido(true);
        if (historialRecibo.getMontoRecibido() == null && historialRecibo.getTotal() != null) {
            historialRecibo.setMontoRecibido(historialRecibo.getTotal());
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
        historialReciboDetalleService.create(detalle);

        // 4) Línea de pago (fuente del corte) — un medio
        if (saved.getMetodoPagoId() == null) {
            throw new IllegalArgumentException("Ticket rápido requiere metodoPagoId.");
        }
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ticket rápido requiere total > 0.");
        }
        historialReciboPagoRepository.save(HistorialReciboPago.builder()
                .historialReciboId(saved.getId())
                .metodoPagoId(saved.getMetodoPagoId())
                .monto(subtotal)
                .orden((short) 1)
                .build());

        // 5) Documento VTA- (trazabilidad)
        DocumentoVenta doc = documentoVentaService.crearDesdeHistorialRecibo(
                saved, SecurityContextHelper.getUserId());
        if (doc != null) {
            saved.setDocumentoVentaId(doc.getId());
            saved.setDocumentoVentaConsecutivo(doc.getConsecutivo());
        }

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
        HistorialRecibo found = opt.orElse(null);
        if (found != null) {
            enrichDocumentoVentaConsecutivos(List.of(found));
        }
        return found;
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
        Long estadoAnterior = existing.getEstadoId();
        existing.setClienteId(historialRecibo.getClienteId());
        existing.setEstadoId(historialRecibo.getEstadoId());
        existing.setMetodoPagoId(historialRecibo.getMetodoPagoId());
        if (historialRecibo.getSesionId() != null) {
            existing.setSesionId(historialRecibo.getSesionId());
        }
        existing.setTotal(historialRecibo.getTotal());
        existing.setMontoRecibido(historialRecibo.getMontoRecibido());

        if (ReciboEstado.ANULADO.getId().equals(historialRecibo.getEstadoId())
                && ReciboEstado.PAGADO.getId().equals(estadoAnterior)) {
            procesarAnulacionConNotaCredito(existing, "ANULACION_ADMIN", null, false);
        }

        HistorialRecibo saved = repository.save(existing);
        enrichDocumentoVentaConsecutivos(List.of(saved));
        return saved;
    }

    private DocumentoVenta resolveDocumentoVenta(HistorialRecibo historial) {
        Optional<DocumentoVenta> docOpt = documentoVentaRepository.findByHistorialReciboId(historial.getId());
        if (docOpt.isPresent()) {
            return docOpt.get();
        }
        if (historial.getDocumentoVentaId() != null) {
            return documentoVentaRepository.findById(historial.getDocumentoVentaId()).orElse(null);
        }
        try {
            return documentoVentaService.crearDesdeHistorialRecibo(historial, SecurityContextHelper.getUserId());
        } catch (Exception e) {
            return null;
        }
    }

    private void procesarAnulacionConNotaCredito(
            HistorialRecibo historial,
            String motivoCodigo,
            String motivoTexto,
            boolean operacionRestauracion) {
        DocumentoVenta documento = resolveDocumentoVenta(historial);
        if (documento == null || documento.getEstado() == DocumentoVentaEstado.ANULADO) {
            return;
        }
        NotaAjusteDocumento nota = notaAjusteService.crearNotaCreditoAnulacion(
                documento,
                historial,
                motivoCodigo,
                motivoTexto,
                operacionRestauracion,
                SecurityContextHelper.getUserId());

        try {
            List<HistorialReciboDetalle> detalles =
                    historialReciboDetalleService.findEntityListByReciboId(historial.getId());
            movimientoInventarioService.registrarReintegroVenta(
                    historial, nota, detalles, SecurityContextHelper.getUserId());
        } catch (Exception e) {
            // Sprint 3 SQL pendiente: anulación no debe fallar por kardex.
        }
    }

    @Override
    public HistorialDocumentosDto getDocumentos(Long historialReciboId) {
        return notaAjusteService.getDocumentosByHistorialReciboId(historialReciboId);
    }

    @Override
    @Transactional
    public RestaurarTicketResponseDto restaurarTicket(
            Long historialReciboId,
            Long sesionId,
            MotivoOperacionRequestDto request) {
        if (historialReciboId == null || sesionId == null) {
            throw new IllegalArgumentException("historialReciboId y sesionId son obligatorios");
        }
        HistorialRecibo historial = findById(historialReciboId);
        if (historial == null) {
            throw new IllegalArgumentException("Historial no encontrado");
        }
        if (!ReciboEstado.PAGADO.getId().equals(historial.getEstadoId())) {
            throw new IllegalStateException("Solo se puede restaurar una venta pagada");
        }

        DocumentoVenta documento = resolveDocumentoVenta(historial);
        if (documento == null) {
            throw new IllegalStateException("No hay documento de venta asociado");
        }
        String docConsecutivo = documento.getConsecutivo();

        String motivoCodigo = request != null && request.getMotivoOperacionCodigo() != null
                ? request.getMotivoOperacionCodigo()
                : "RESTAURACION_TICKET";
        String motivoTexto = request != null ? request.getMotivoTexto() : null;

        NotaAjusteDocumento nota = notaAjusteService.crearNotaCreditoAnulacion(
                documento,
                historial,
                motivoCodigo,
                motivoTexto,
                true,
                SecurityContextHelper.getUserId());

        try {
            List<HistorialReciboDetalle> detalles =
                    historialReciboDetalleService.findEntityListByReciboId(historial.getId());
            movimientoInventarioService.registrarReintegroVenta(
                    historial, nota, detalles, SecurityContextHelper.getUserId());
        } catch (Exception e) {
            // Sprint 3 SQL pendiente: restauración no debe fallar por kardex.
        }

        moveToEdition(historialReciboId, sesionId);

        Sesion sesion = sesionService.findById(sesionId);
        Long ticketId = sesion != null ? sesion.getUltimoTicketId() : null;
        Long reciboId = null;
        if (ticketId != null) {
            List<TicketRecibo> links = ticketReciboService.findByTicketId(ticketId);
            if (links != null && !links.isEmpty()) {
                reciboId = links.get(0).getReciboId();
            }
        }

        return RestaurarTicketResponseDto.builder()
                .notaCreditoConsecutivo(nota.getConsecutivo())
                .notaAjusteId(nota.getId())
                .documentoVentaConsecutivoAnulado(docConsecutivo)
                .ticketId(ticketId)
                .reciboId(reciboId)
                .build();
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        historialReciboPagoRepository.deleteByHistorialReciboId(id);
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
        return search(fecha, estadoId, 0L, null, null, null, null, pageable);
    }

    @Override
    public Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Pageable pageable) {
        return search(fecha, estadoId, sesionId, null, null, null, null, pageable);
    }

    @Override
    public Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Boolean soloRestaurados, Pageable pageable) {
        return search(fecha, estadoId, sesionId, soloRestaurados, null, null, null, pageable);
    }

    @Override
    public Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Boolean soloRestaurados,
                                        Long metodoPagoId, Boolean mixto, Boolean sinCorte, Pageable pageable) {
        return search(fecha, estadoId, sesionId, soloRestaurados, metodoPagoId, mixto, sinCorte, null, null, pageable);
    }

    @Override
    public Page<HistorialRecibo> search(String fecha, Long estadoId, Long sesionId, Boolean soloRestaurados,
                                        Long metodoPagoId, Boolean mixto, Boolean sinCorte,
                                        Long clienteId, Long productoId, Pageable pageable) {
        if (Boolean.TRUE.equals(soloRestaurados)) {
            Page<HistorialRecibo> page = searchRestaurados(fecha, sesionId, pageable);
            enrichDocumentoVentaConsecutivos(page.getContent());
            enrichMultipagoFlags(page.getContent());
            enrichNotificacionElectronica(page.getContent());
            enrichSesionUserIds(page.getContent());
            return filterPageInMemory(page, metodoPagoId, mixto, sinCorte, pageable);
        }
        return searchInternal(fecha, estadoId, sesionId, metodoPagoId, mixto, sinCorte, clienteId, productoId, pageable);
    }

    private Page<HistorialRecibo> searchInternal(String fecha, Long estadoId, Long sesionId,
                                                 Long metodoPagoId, Boolean mixto, Boolean sinCorte,
                                                 Long clienteId, Long productoId,
                                                 Pageable pageable) {
        boolean hasSesion = sesionId != null && sesionId > 0;
        Specification<HistorialRecibo> spec = buildSearchSpec(fecha, estadoId, sesionId, metodoPagoId, mixto, sinCorte, clienteId, productoId);

        if (hasSesion) {
            // Compat: por sesión se devolvía lista completa (sin paginar).
            List<HistorialRecibo> results = repository.findAll(spec);
            enrichDocumentoVentaConsecutivos(results);
            enrichMultipagoFlags(results);
            enrichNotificacionElectronica(results);
            enrichSesionUserIds(results);
            return new PageImpl<>(results);
        }

        Page<HistorialRecibo> page = repository.findAll(spec, pageable);
        enrichDocumentoVentaConsecutivos(page.getContent());
        enrichMultipagoFlags(page.getContent());
        enrichNotificacionElectronica(page.getContent());
        enrichSesionUserIds(page.getContent());
        return page;
    }

    private Specification<HistorialRecibo> buildSearchSpec(String fecha, Long estadoId, Long sesionId,
                                                           Long metodoPagoId, Boolean mixto, Boolean sinCorte,
                                                           Long clienteId, Long productoId) {
        return (root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> preds = new ArrayList<>();
            boolean hasFecha = fecha != null && !fecha.isBlank();
            boolean hasEstado = estadoId != null && estadoId != 0;
            boolean hasSesion = sesionId != null && sesionId > 0;

            if (hasSesion) {
                preds.add(cb.equal(root.get("sesionId"), sesionId));
            }
            if (hasFecha) {
                try {
                    LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                    preds.add(cb.between(root.get("fechaCreacion"), startOfDay, endOfDay));
                } catch (Exception ignored) {
                    preds.add(cb.disjunction());
                }
            }
            if (hasEstado) {
                preds.add(cb.equal(root.get("estadoId"), estadoId));
            }
            if (Boolean.TRUE.equals(sinCorte)) {
                Long afterId = resolveUltimoHistorialReciboWatermark();
                preds.add(cb.greaterThan(root.get("id"), afterId));
            }
            if (Boolean.TRUE.equals(mixto)) {
                Subquery<Long> countSq = query.subquery(Long.class);
                Root<HistorialReciboPago> p = countSq.from(HistorialReciboPago.class);
                countSq.select(cb.count(p));
                countSq.where(cb.equal(p.get("historialReciboId"), root.get("id")));
                preds.add(cb.greaterThan(countSq, 1L));
            } else if (metodoPagoId != null && metodoPagoId > 0) {
                Subquery<Long> pagoSq = query.subquery(Long.class);
                Root<HistorialReciboPago> p = pagoSq.from(HistorialReciboPago.class);
                pagoSq.select(p.get("historialReciboId"));
                pagoSq.where(
                        cb.equal(p.get("historialReciboId"), root.get("id")),
                        cb.equal(p.get("metodoPagoId"), metodoPagoId));
                preds.add(cb.or(
                        cb.equal(root.get("metodoPagoId"), metodoPagoId),
                        cb.exists(pagoSq)));
            }
            if (clienteId != null && clienteId > 0) {
                preds.add(cb.equal(root.get("clienteId"), clienteId));
            }
            if (productoId != null && productoId > 0) {
                Subquery<Long> detSq = query.subquery(Long.class);
                Root<HistorialReciboDetalle> d = detSq.from(HistorialReciboDetalle.class);
                detSq.select(d.get("reciboId"));
                detSq.where(
                        cb.equal(d.get("reciboId"), root.get("id")),
                        cb.equal(d.get("productoId"), productoId));
                preds.add(cb.exists(detSq));
            }
            if (preds.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(preds.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }

    private Long resolveUltimoHistorialReciboWatermark() {
        return corteVentaRepository.findFirstByEstadoNotOrderByIdDesc("eliminado")
                .map(c -> c.getUltimoHistorialReciboId() != null ? c.getUltimoHistorialReciboId() : 0L)
                .orElse(0L);
    }

    private void enrichMultipagoFlags(List<HistorialRecibo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Long> ids = items.stream().map(HistorialRecibo::getId).filter(id -> id != null).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : historialReciboPagoRepository.countByHistorialReciboIdIn(ids)) {
            if (row[0] != null) {
                counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }
        for (HistorialRecibo item : items) {
            long n = counts.getOrDefault(item.getId(), 0L);
            // Sin filas de pago: legacy 1 medio (header). Con 2+ filas: mixto.
            item.setMultipago(n > 1);
        }
    }

    private void enrichNotificacionElectronica(List<HistorialRecibo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Long> ids = items.stream().map(HistorialRecibo::getId).filter(id -> id != null).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> estados = new HashMap<>();
        try {
            for (HistorialReciboElectronico hre : historialReciboElectronicoRepository.findByHistorialReciboIdIn(ids)) {
                if (hre.getHistorialReciboId() != null && hre.getEstado() != null) {
                    estados.put(hre.getHistorialReciboId(), hre.getEstado());
                }
            }
        } catch (Exception ignored) {
            // Schema HRE incompleto (p.ej. sin nombre_cliente): la lista de tickets no debe fallar.
            return;
        }
        for (HistorialRecibo item : items) {
            item.setEstadoNotificacionElectronica(estados.get(item.getId()));
        }
    }

    private void enrichSesionUserIds(List<HistorialRecibo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> sesionIds = items.stream()
                .map(HistorialRecibo::getSesionId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (sesionIds.isEmpty()) {
            return;
        }
        Map<Long, java.util.UUID> userBySesion = new HashMap<>();
        for (Sesion sesion : sesionRepository.findAllById(sesionIds)) {
            if (sesion.getId() != null && sesion.getUserId() != null) {
                userBySesion.put(sesion.getId(), sesion.getUserId());
            }
        }
        for (HistorialRecibo item : items) {
            item.setSesionUserId(userBySesion.get(item.getSesionId()));
        }
    }

    /**
     * Filtros extra sobre página ya armada (p.ej. restaurados).
     */
    private Page<HistorialRecibo> filterPageInMemory(Page<HistorialRecibo> page,
                                                     Long metodoPagoId, Boolean mixto, Boolean sinCorte,
                                                     Pageable pageable) {
        boolean needFilter = Boolean.TRUE.equals(mixto)
                || Boolean.TRUE.equals(sinCorte)
                || (metodoPagoId != null && metodoPagoId > 0);
        if (!needFilter) {
            return page;
        }
        Long afterId = Boolean.TRUE.equals(sinCorte) ? resolveUltimoHistorialReciboWatermark() : null;
        Set<Long> matchMetodo = null;
        if (!Boolean.TRUE.equals(mixto) && metodoPagoId != null && metodoPagoId > 0) {
            matchMetodo = new HashSet<>();
            for (HistorialRecibo h : page.getContent()) {
                if (metodoPagoId.equals(h.getMetodoPagoId())) {
                    matchMetodo.add(h.getId());
                    continue;
                }
                boolean has = historialReciboPagoRepository
                        .findByHistorialReciboIdOrderByOrdenAsc(h.getId())
                        .stream()
                        .anyMatch(p -> metodoPagoId.equals(p.getMetodoPagoId()));
                if (has) {
                    matchMetodo.add(h.getId());
                }
            }
        }
        Set<Long> finalMatchMetodo = matchMetodo;
        List<HistorialRecibo> filtered = page.getContent().stream()
                .filter(h -> {
                    if (afterId != null && (h.getId() == null || h.getId() <= afterId)) {
                        return false;
                    }
                    if (Boolean.TRUE.equals(mixto) && !Boolean.TRUE.equals(h.getMultipago())) {
                        return false;
                    }
                    if (finalMatchMetodo != null && !finalMatchMetodo.contains(h.getId())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    private Page<HistorialRecibo> searchRestaurados(String fecha, Long sesionId, Pageable pageable) {
        boolean hasFecha = fecha != null && !fecha.isBlank();
        boolean hasSesion = sesionId != null && sesionId > 0;
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;
        if (hasFecha) {
            try {
                LocalDate date = LocalDate.parse(fecha, DATE_FMT);
                startOfDay = date.atStartOfDay();
                endOfDay = date.atTime(LocalTime.MAX);
            } catch (Exception e) {
                return Page.empty(pageable);
            }
        }

        List<HistorialRecibo> items = new ArrayList<>();
        for (NotaAjusteDocumento nota : notaAjusteDocumentoRepository.findByOperacionRestauracionTrueOrderByFechaHechoDesc()) {
            Long hrId = nota.getHistorialReciboId();
            if (hrId == null) {
                continue;
            }
            HistorialRecibo hr = repository.findById(hrId).orElse(null);
            if (hr == null) {
                hr = edicionReciboRepository.findByHistorialReciboId(hrId)
                        .map(this::toHistorialFromEdicion)
                        .orElse(null);
                if (hr != null) {
                    hr.setId(hrId);
                }
            }
            if (hr == null) {
                continue;
            }
            if (hasSesion && !sesionId.equals(hr.getSesionId())) {
                continue;
            }
            if (hasFecha && hr.getFechaCreacion() != null) {
                LocalDateTime fc = hr.getFechaCreacion();
                if (fc.isBefore(startOfDay) || fc.isAfter(endOfDay)) {
                    continue;
                }
            }
            hr.setRestaurado(true);
            items.add(hr);
        }

        enrichDocumentoVentaConsecutivos(items);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<HistorialRecibo> pageContent = start >= items.size() ? List.of() : items.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, items.size());
    }

    private HistorialRecibo toHistorialFromEdicion(EdicionRecibo edicion) {
        return HistorialRecibo.builder()
                .clienteId(edicion.getClienteId())
                .fechaCreacion(edicion.getFechaCreacion())
                .estadoId(edicion.getEstadoId())
                .metodoPagoId(edicion.getMetodoPagoId())
                .sesionId(edicion.getSesionId())
                .total(edicion.getTotal())
                .montoRecibido(edicion.getMontoRecibido())
                .build();
    }

    private void enrichDocumentoVentaConsecutivos(List<HistorialRecibo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        List<Long> historialIds = items.stream()
                .map(HistorialRecibo::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (historialIds.isEmpty()) {
            return;
        }

        try {
            Map<Long, String> consecutivos = documentoVentaRepository.findByHistorialReciboIdIn(historialIds).stream()
                    .filter(dv -> dv.getHistorialReciboId() != null && dv.getConsecutivo() != null)
                    .collect(Collectors.toMap(
                            DocumentoVenta::getHistorialReciboId,
                            DocumentoVenta::getConsecutivo,
                            (a, b) -> a
                    ));

            for (HistorialRecibo item : items) {
                if (item.getDocumentoVentaConsecutivo() != null && !item.getDocumentoVentaConsecutivo().isBlank()) {
                    continue;
                }
                String consecutivo = consecutivos.get(item.getId());
                if (consecutivo != null) {
                    item.setDocumentoVentaConsecutivo(consecutivo);
                }
            }
        } catch (Exception ignored) {
            // Tablas Sprint 0 aún no aplicadas: historial sigue sin consecutivo.
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
                .montoRecibido(historial.getMontoRecibido())
                .build();
        Recibo reciboGuardado = reciboService.saveAndFlush(nuevoRecibo);

        // 2. Copy HistorialRecibo to EdicionRecibo
        EdicionRecibo edicionRecibo = EdicionRecibo.builder()
                .reciboId(reciboGuardado.getId())
                .historialReciboId(historial.getId())
                .clienteId(historial.getClienteId())
                .estadoId(historial.getEstadoId())
                .metodoPagoId(historial.getMetodoPagoId())
                .sesionId(historial.getSesionId())
                .total(historial.getTotal())
                .montoRecibido(historial.getMontoRecibido())
                .build();
        EdicionRecibo edicionReciboGuardado = edicionReciboService.create(edicionRecibo);

        // 3. Move and Copy Details
        List<HistorialReciboDetalle> detallesHistorial = historialReciboDetalleService.findEntityListByReciboId(historialReciboId);
        for (HistorialReciboDetalle detalleHistorial : detallesHistorial) {
            // a. Move to ReciboDetalle
            ReciboDetalle nuevoDetalle = ReciboDetalle.builder()
                    .reciboId(reciboGuardado.getId())
                    .productoId(detalleHistorial.getProductoId())
                    .presentacionId(detalleHistorial.getPresentacionId())
                    .cantidad(detalleHistorial.getCantidad())
                    .cantidadBase(detalleHistorial.getCantidadBase())
                    .precioUnitarioSnapshot(detalleHistorial.getPrecioUnitarioSnapshot())
                    .factorSnapshot(detalleHistorial.getFactorSnapshot())
                    .subtotal(detalleHistorial.getSubtotal())
                    .fechaCreacion(detalleHistorial.getFechaCreacion())
                    .usuarioCreacion(detalleHistorial.getUsuarioCreacion())
                    .build();
            reciboDetalleService.create(nuevoDetalle);

            // b. Copy to EdicionReciboDetalle
            EdicionReciboDetalle edicionDetalle = EdicionReciboDetalle.builder()
                    .edicionId(edicionReciboGuardado.getId())
                    .productoId(detalleHistorial.getProductoId())
                    .presentacionId(detalleHistorial.getPresentacionId())
                    .cantidad(detalleHistorial.getCantidad())
                    .cantidadBase(detalleHistorial.getCantidadBase())
                    .precioUnitarioSnapshot(detalleHistorial.getPrecioUnitarioSnapshot())
                    .factorSnapshot(detalleHistorial.getFactorSnapshot())
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

        // 7. Delete original HistorialRecibo and its details
        historialReciboDetalleService.deleteByReciboId(historialReciboId);
        historialReciboPagoRepository.deleteByHistorialReciboId(historialReciboId);
        repository.delete(historial);
    }

    @Override
    public List<ReciboPagoLineaDto> findPagosByHistorialId(Long historialReciboId) {
        if (historialReciboId == null) {
            return List.of();
        }
        return historialReciboPagoRepository.findByHistorialReciboIdOrderByOrdenAsc(historialReciboId)
                .stream()
                .map(p -> ReciboPagoLineaDto.builder()
                        .metodoPagoId(p.getMetodoPagoId())
                        .monto(p.getMonto())
                        .build())
                .collect(Collectors.toList());
    }
}
