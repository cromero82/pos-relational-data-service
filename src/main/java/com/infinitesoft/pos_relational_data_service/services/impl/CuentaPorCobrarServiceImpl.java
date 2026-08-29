package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.AbonoCxcDto;
import com.infinitesoft.pos_relational_data_service.dto.AbrirCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CastigoCarteraInventarioResult;
import com.infinitesoft.pos_relational_data_service.dto.CerrarCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CuentaPorCobrarDto;
import com.infinitesoft.pos_relational_data_service.dto.MovimientoOrigenFondosDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboPagoLineaDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboPagoResponseDto;
import com.infinitesoft.pos_relational_data_service.dto.RegistrarAbonoCxcRequest;
import com.infinitesoft.pos_relational_data_service.dto.SincronizarCxCTicketRequest;
import com.infinitesoft.pos_relational_data_service.entities.AbonoCxc;
import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.entities.CuentaPorCobrar;
import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboElectronico;
import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;
import com.infinitesoft.pos_relational_data_service.entities.MotivoOperacion;
import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.entities.TicketRecibo;
import com.infinitesoft.pos_relational_data_service.repositories.AbonoCxcRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ClientRepository;
import com.infinitesoft.pos_relational_data_service.repositories.CuentaPorCobrarRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboElectronicoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MotivoOperacionRepository;
import com.infinitesoft.pos_relational_data_service.repositories.OrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.SesionRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.CuentaPorCobrarService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoInventarioService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleService;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import com.infinitesoft.pos_relational_data_service.services.TicketService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CuentaPorCobrarServiceImpl implements CuentaPorCobrarService {

    private static final List<String> ESTADOS_VIGENTES = Arrays.asList("ABIERTA", "PARCIAL");
    private static final List<String> ESTADOS_ARCHIVADOS = Arrays.asList("PAGADA", "ANULADA", "CASTIGADA");
    private static final String MOTIVO_ANULAR = "CXC_ANULAR_SIN_ABONOS";
    private static final String MOTIVO_CASTIGO = "CXC_CASTIGO_CARTERA";

    @Autowired
    private CuentaPorCobrarRepository cuentaPorCobrarRepository;

    @Autowired
    private AbonoCxcRepository abonoCxcRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ReciboRepository reciboRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketReciboRepository ticketReciboRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private OrigenFondosRepository origenFondosRepository;

    @Autowired
    private MotivoOperacionRepository motivoOperacionRepository;

    @Autowired
    private HistorialReciboElectronicoRepository historialReciboElectronicoRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private MovimientoOrigenFondosService movimientoOrigenFondosService;

    @Autowired
    private ReciboDetalleService reciboDetalleService;

    @Autowired
    private MovimientoInventarioService movimientoInventarioService;

    @Autowired
    @Lazy
    private ReciboService reciboService;

    @Autowired
    private TicketService ticketService;

    @Override
    @Transactional
    public CuentaPorCobrarDto abrirDesdeTicket(AbrirCuentaPorCobrarRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Solicitud obligatoria.");
        }
        if (request.getReciboId() == null) {
            throw new IllegalArgumentException("Debe indicar el recibo del ticket.");
        }
        if (request.getClienteId() == null) {
            throw new IllegalArgumentException("Debe asociar un cliente identificado.");
        }
        if (request.getMonto() != null && request.getMonto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo a crédito no puede ser negativo.");
        }

        String telefono = trimToNull(request.getTelefono());
        if (telefono == null) {
            throw new IllegalArgumentException("El teléfono del cliente es obligatorio para abrir crédito.");
        }

        Recibo recibo = reciboRepository.findById(request.getReciboId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recibo no encontrado: " + request.getReciboId()));

        cuentaPorCobrarRepository
                .findFirstByReciboIdAndEstadoInOrderByIdDesc(recibo.getId(), ESTADOS_VIGENTES)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Este ticket ya tiene una cuenta por cobrar vigente (#" + existing.getId() + ").");
                });

        Client cliente = clientRepository.findById(request.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado: " + request.getClienteId()));

        String nombre = trimToNull(request.getClienteNombre());
        if (nombre == null) {
            nombre = cliente.getNombre();
        }
        if (nombre == null || nombre.isBlank() || "ANONIMO".equalsIgnoreCase(nombre.trim())) {
            throw new IllegalArgumentException(
                    "No se puede abrir crédito a cliente anónimo. Identifique al cliente primero.");
        }

        cliente.setNombre(nombre.trim());
        cliente.setTelefono(telefono);
        cliente.setCorreo(trimToNull(request.getCorreo()));
        if (trimToNull(request.getDocumento()) != null) {
            cliente.setDocumento(request.getDocumento().trim());
        }
        clientRepository.save(cliente);

        if (!request.getClienteId().equals(recibo.getClienteId())) {
            recibo.setClienteId(request.getClienteId());
        }

        // Total del ticket: FE manda la suma de productos; en BD el recibo vivo
        // a menudo sigue en 0 hasta el cobro.
        BigDecimal totalTicket = request.getTotalTicket() != null
                ? request.getTotalTicket()
                : BigDecimal.ZERO;
        if (totalTicket.compareTo(BigDecimal.ZERO) <= 0
                && recibo.getTotal() != null
                && recibo.getTotal().compareTo(BigDecimal.ZERO) > 0) {
            totalTicket = recibo.getTotal();
        }
        if (totalTicket.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El ticket no tiene total. Agregue productos antes de generar crédito.");
        }
        if (recibo.getTotal() == null || recibo.getTotal().compareTo(totalTicket) != 0) {
            recibo.setTotal(totalTicket);
        }
        reciboRepository.save(recibo);

        BigDecimal abono = request.getAbono() != null ? request.getAbono() : BigDecimal.ZERO;
        if (abono.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El abono no puede ser negativo.");
        }
        if (abono.compareTo(totalTicket) > 0) {
            throw new IllegalArgumentException(
                    "El abono (" + abono + ") no puede superar el total del ticket (" + totalTicket + ").");
        }

        BigDecimal saldoCredito = request.getMonto();
        if (saldoCredito == null) {
            saldoCredito = totalTicket.subtract(abono);
        }
        if (saldoCredito.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El saldo a crédito debe ser mayor a cero. Si paga el total, no abra cuenta por cobrar.");
        }
        BigDecimal suma = abono.add(saldoCredito);
        if (suma.compareTo(totalTicket) > 0) {
            throw new IllegalArgumentException(
                    "Abono + saldo a crédito (" + suma + ") supera el total del ticket (" + totalTicket + ").");
        }

        if (request.getTicketId() != null && !ticketRepository.existsById(request.getTicketId())) {
            throw new IllegalArgumentException("Ticket no encontrado: " + request.getTicketId());
        }

        CuentaPorCobrar entity = CuentaPorCobrar.builder()
                .reciboId(recibo.getId())
                .ticketId(request.getTicketId())
                .clienteId(cliente.getId())
                .montoOriginal(saldoCredito)
                .saldoPendiente(saldoCredito)
                .totalTicket(totalTicket)
                .estado("ABIERTA")
                .observacion(buildObservacionApertura(trimToNull(request.getObservacion()), totalTicket, abono, saldoCredito))
                .usuarioId(SecurityContextHelper.getUserId())
                .build();

        CuentaPorCobrar saved = cuentaPorCobrarRepository.save(entity);

        if (abono.compareTo(BigDecimal.ZERO) > 0) {
            if (request.getMetodoPagoId() == null) {
                throw new IllegalArgumentException(
                        "Debe indicar el medio de pago del abono inicial.");
            }
            registrarAbonoInterno(
                    saved,
                    abono,
                    request.getMetodoPagoId(),
                    request.getOrigenFondosId(),
                    "Abono inicial al abrir CxC #" + saved.getId(),
                    request.getSesionId(),
                    request.getHistorialElectronicoId(),
                    saved.getClienteId(),
                    cliente != null ? cliente.getNombre() : null
            );
        }

        log.info(
                "CxC abierta id={} recibo={} ticket={} cliente={} total={} abono={} saldoCredito={}",
                saved.getId(), saved.getReciboId(), saved.getTicketId(),
                saved.getClienteId(), totalTicket, abono, saldoCredito);
        return toDto(saved, cliente);
    }

    private static String buildObservacionApertura(
            String observacion,
            BigDecimal totalTicket,
            BigDecimal abono,
            BigDecimal saldoCredito
    ) {
        String base = "Apertura CxC · Total ticket " + totalTicket
                + " · Abono " + abono
                + " · Saldo crédito " + saldoCredito;
        if (observacion == null) {
            return base;
        }
        return observacion + " · " + base;
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaPorCobrarDto findById(Long id) {
        return cuentaPorCobrarRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrarDto> listarVigentes() {
        return cuentaPorCobrarRepository.findByEstadoInOrderByFechaOrigenDesc(ESTADOS_VIGENTES)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrarDto> listarArchivados() {
        return cuentaPorCobrarRepository.findByEstadoInOrderByFechaOrigenDesc(ESTADOS_ARCHIVADOS)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaPorCobrarDto findVigentePorRecibo(Long reciboId) {
        if (reciboId == null) {
            return null;
        }
        return cuentaPorCobrarRepository
                .findFirstByReciboIdAndEstadoInOrderByIdDesc(reciboId, ESTADOS_VIGENTES)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaPorCobrarDto findVigentePorTicket(Long ticketId) {
        if (ticketId == null) {
            return null;
        }
        return cuentaPorCobrarRepository
                .findFirstByTicketIdAndEstadoInOrderByIdDesc(ticketId, ESTADOS_VIGENTES)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public CuentaPorCobrarDto sincronizarTotalTicket(Long ticketId, SincronizarCxCTicketRequest request) {
        if (ticketId == null) {
            throw new IllegalArgumentException("Debe indicar el ticket.");
        }
        if (request == null || request.getTotalTicket() == null) {
            throw new IllegalArgumentException("Debe indicar el total del ticket.");
        }
        BigDecimal nuevoTotal = request.getTotalTicket();
        if (nuevoTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total del ticket no puede ser negativo.");
        }

        CuentaPorCobrar cxc = cuentaPorCobrarRepository
                .findFirstByTicketIdAndEstadoInOrderByIdDesc(ticketId, ESTADOS_VIGENTES)
                .orElse(null);
        if (cxc == null) {
            return null;
        }

        BigDecimal totalAnterior = cxc.getTotalTicket();
        if (totalAnterior == null) {
            totalAnterior = cxc.getMontoOriginal() != null ? cxc.getMontoOriginal() : BigDecimal.ZERO;
        }

        BigDecimal delta = nuevoTotal.subtract(totalAnterior);
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            if (cxc.getTotalTicket() == null) {
                cxc.setTotalTicket(nuevoTotal);
                cxc = cuentaPorCobrarRepository.save(cxc);
            }
            return toDto(cxc);
        }

        BigDecimal original = cxc.getMontoOriginal() != null ? cxc.getMontoOriginal() : BigDecimal.ZERO;
        BigDecimal saldo = cxc.getSaldoPendiente() != null ? cxc.getSaldoPendiente() : BigDecimal.ZERO;
        BigDecimal abonado = original.subtract(saldo);
        if (abonado.compareTo(BigDecimal.ZERO) < 0) {
            abonado = BigDecimal.ZERO;
        }

        BigDecimal nuevoSaldo = saldo.add(delta);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            nuevoSaldo = BigDecimal.ZERO;
        }
        BigDecimal nuevoOriginal = abonado.add(nuevoSaldo);

        // ck_cxc_montos: monto_original > 0
        if (nuevoOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            log.info(
                    "CxC sync ticket={} total={} delta={}: sin monto crédito positivo; solo actualiza total_ticket",
                    ticketId, nuevoTotal, delta);
            cxc.setTotalTicket(nuevoTotal);
            if (cxc.getReciboId() != null) {
                reciboRepository.findById(cxc.getReciboId()).ifPresent(r -> {
                    r.setTotal(nuevoTotal);
                    reciboRepository.save(r);
                });
            }
            return toDto(cuentaPorCobrarRepository.save(cxc));
        }

        cxc.setMontoOriginal(nuevoOriginal);
        cxc.setSaldoPendiente(nuevoSaldo);
        cxc.setTotalTicket(nuevoTotal);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) > 0) {
            cxc.setEstado(abonado.compareTo(BigDecimal.ZERO) > 0 ? "PARCIAL" : "ABIERTA");
        }
        // saldo 0 por reducción de ticket: no marcar PAGADA (sigue vigente si re-agrega ítems)

        if (cxc.getReciboId() != null) {
            Recibo recibo = reciboRepository.findById(cxc.getReciboId()).orElse(null);
            if (recibo != null) {
                recibo.setTotal(nuevoTotal);
                reciboRepository.save(recibo);
            }
        }

        CuentaPorCobrar saved = cuentaPorCobrarRepository.save(cxc);
        log.info(
                "CxC sync id={} ticket={} total {}→{} delta={} original={} saldo={}",
                saved.getId(), ticketId, totalAnterior, nuevoTotal, delta,
                nuevoOriginal, nuevoSaldo);
        return toDto(saved);
    }

    @Override
    @Transactional
    public AbonoCxcDto registrarAbono(Long cuentaId, RegistrarAbonoCxcRequest request) {
        if (cuentaId == null) {
            throw new IllegalArgumentException("Debe indicar la cuenta por cobrar.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Solicitud de abono obligatoria.");
        }
        BigDecimal monto = request.getMonto();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor a cero.");
        }
        if (request.getMetodoPagoId() == null) {
            throw new IllegalArgumentException("Debe indicar el medio de pago del abono.");
        }

        CuentaPorCobrar cxc = cuentaPorCobrarRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cuenta por cobrar no encontrada: " + cuentaId));
        if (!ESTADOS_VIGENTES.contains(cxc.getEstado())) {
            throw new IllegalArgumentException(
                    "La cuenta #" + cuentaId + " no está vigente (estado " + cxc.getEstado() + ").");
        }
        BigDecimal saldo = cxc.getSaldoPendiente() != null ? cxc.getSaldoPendiente() : BigDecimal.ZERO;
        if (monto.compareTo(saldo) > 0) {
            throw new IllegalArgumentException(
                    "El abono (" + monto + ") supera el saldo pendiente (" + saldo + ").");
        }

        AbonoCxc savedAbono = registrarAbonoInterno(
                cxc,
                monto,
                request.getMetodoPagoId(),
                request.getOrigenFondosId(),
                trimToNull(request.getObservacion()),
                request.getSesionId(),
                null,
                request.getClientePagadorId(),
                trimToNull(request.getClientePagadorNombre())
        );

        BigDecimal nuevoSaldo = saldo.subtract(monto);
        cxc.setSaldoPendiente(nuevoSaldo);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            cxc.setEstado("PAGADA");
            formalizarTicketSiLiquidada(cxc);
        } else {
            cxc.setEstado("PARCIAL");
            cuentaPorCobrarRepository.save(cxc);
        }

        MetodoPago mp = metodoPagoRepository.findById(savedAbono.getMetodoPagoId()).orElse(null);
        boolean pendienteQr = savedAbono.getId() != null
                && historialReciboElectronicoRepository.findByAbonoCxcId(savedAbono.getId()).isPresent();
        log.info(
                "Abono CxC id={} cuenta={} monto={} saldoNuevo={} estado={} pendienteQr={}",
                savedAbono.getId(), cxc.getId(), monto, nuevoSaldo, cxc.getEstado(), pendienteQr);
        return toAbonoDto(savedAbono, mp, pendienteQr);
    }

    /**
     * Persiste abono + ENTRADA_COBRANZA. Si {@code aplicarASaldo} es false (abono inicial),
     * no toca saldo/estado: ya quedaron fijados al abrir.
     */
    private AbonoCxc registrarAbonoInterno(
            CuentaPorCobrar cxc,
            BigDecimal monto,
            Long metodoPagoId,
            Integer origenFondosId,
            String observacion,
            Long sesionIdCaja,
            Long historialElectronicoIdConfirmado,
            Long clientePagadorId,
            String clientePagadorNombre
    ) {
        MetodoPago mp = metodoPagoRepository.findById(metodoPagoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Método de pago no encontrado: " + metodoPagoId));

        OrigenFondos origen;
        if (origenFondosId != null) {
            origen = origenFondosRepository.findById(origenFondosId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Origen de fondos no encontrado: " + origenFondosId));
        } else {
            origen = origenFondosRepository.findByMetodoPagoId(mp.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No hay origen de fondos raíz para el medio «"
                                    + mp.getDescripcion() + "»."));
        }

        Long pagadorId = clientePagadorId != null ? clientePagadorId : cxc.getClienteId();
        Client pagador = pagadorId != null
                ? clientRepository.findById(pagadorId).orElse(null)
                : null;
        String pagadorNombre = clientePagadorNombre;
        if (pagadorNombre == null || pagadorNombre.isBlank()) {
            pagadorNombre = pagador != null ? pagador.getNombre() : null;
        }
        if ((pagadorNombre == null || pagadorNombre.isBlank()) && cxc.getClienteId() != null) {
            Client deudor = clientRepository.findById(cxc.getClienteId()).orElse(null);
            pagadorNombre = deudor != null ? deudor.getNombre() : null;
        }

        String tercero = pagadorNombre;
        String obs = observacion;
        if (obs == null || obs.isBlank()) {
            obs = "Abono CxC #" + cxc.getId()
                    + (tercero != null ? " · " + tercero : "");
        }

        AbonoCxc abono = AbonoCxc.builder()
                .cuentaPorCobrarId(cxc.getId())
                .monto(monto)
                .metodoPagoId(mp.getId())
                .origenFondosId(origen.getId())
                .observacion(obs)
                .usuarioId(SecurityContextHelper.getUserId())
                .clientePagadorId(pagadorId)
                .clientePagadorNombre(pagadorNombre)
                .build();
        AbonoCxc savedAbono = abonoCxcRepository.save(abono);

        MovimientoOrigenFondosDto mov = movimientoOrigenFondosService.registrarEntradaCobranza(
                origen.getId(),
                monto,
                savedAbono.getId(),
                tercero,
                obs
        );
        savedAbono.setMovimientoOrigenFondosId(mov.getId());
        savedAbono = abonoCxcRepository.save(savedAbono);
        if (historialElectronicoIdConfirmado != null) {
            retargetHreConfirmadoAAbono(
                    historialElectronicoIdConfirmado, savedAbono, cxc, mp, sesionIdCaja);
        } else {
            // Abono inicial (abrir CxC) también puede ser QR → pendiente panel.
            registrarPendienteConfirmacionElectronicaAbono(cxc, savedAbono, mp, sesionIdCaja);
        }
        return savedAbono;
    }

    /**
     * Faltante QR venta: el email ya está CONFIRMADA en HRE de la venta;
     * al abrir CxC se mueve el origen XOR a abono (sin crear otro pendiente).
     */
    private void retargetHreConfirmadoAAbono(
            Long historialElectronicoId,
            AbonoCxc abono,
            CuentaPorCobrar cxc,
            MetodoPago mp,
            Long sesionIdCaja
    ) {
        try {
            if (historialElectronicoId == null || abono == null || abono.getId() == null) {
                return;
            }
            HistorialReciboElectronico hre = historialReciboElectronicoRepository
                    .findById(historialElectronicoId)
                    .orElse(null);
            if (hre == null) {
                log.warn("HRE #{} no encontrado para retarget a abono #{}",
                        historialElectronicoId, abono.getId());
                registrarPendienteConfirmacionElectronicaAbono(cxc, abono, mp, sesionIdCaja);
                return;
            }
            hre.setHistorialReciboId(null);
            hre.setAbonoCxcId(abono.getId());
            if (hre.getSesionId() == null) {
                hre.setSesionId(resolverSesionParaPendiente(cxc, sesionIdCaja));
            }
            if (cxc.getClienteId() != null) {
                String nombreCliente = clientRepository.findById(cxc.getClienteId())
                        .map(Client::getNombre)
                        .orElse(null);
                if (nombreCliente != null) {
                    hre.setNombreCliente(nombreCliente);
                }
            }
            historialReciboElectronicoRepository.save(hre);
            log.info("HRE #{} retarget venta→abonoCxC #{} (estado={})",
                    hre.getId(), abono.getId(), hre.getEstado());
        } catch (Exception e) {
            log.warn("No se pudo retarget HRE #{} a abono #{}: {}",
                    historialElectronicoId, abono != null ? abono.getId() : null, e.getMessage());
            registrarPendienteConfirmacionElectronicaAbono(cxc, abono, mp, sesionIdCaja);
        }
    }

    /**
     * Abono CxC QR/Bancolombia: crea pendiente CREADA ligada a {@code abono_cxc_id}
     * (sin historial_recibo hasta liquidar). Best-effort.
     * La sesión debe ser la caja activa (panel), no la histórica del ticket/recibo.
     */
    private boolean registrarPendienteConfirmacionElectronicaAbono(
            CuentaPorCobrar cxc, AbonoCxc abono, MetodoPago mp, Long sesionIdCaja) {
        try {
            if (abono == null || abono.getId() == null || mp == null) {
                return false;
            }
            if (!esMetodoQrElectronico(mp)) {
                return false;
            }
            if (historialReciboElectronicoRepository.findByAbonoCxcId(abono.getId()).isPresent()) {
                return true;
            }
            Long sesionId = resolverSesionParaPendiente(cxc, sesionIdCaja);
            String nombreCliente = null;
            if (cxc.getClienteId() != null) {
                nombreCliente = clientRepository.findById(cxc.getClienteId())
                        .map(Client::getNombre)
                        .orElse(null);
            }
            historialReciboElectronicoRepository.save(HistorialReciboElectronico.builder()
                    .abonoCxcId(abono.getId())
                    .sesionId(sesionId)
                    .metodoPagoId(mp.getId())
                    .montoEsperado(abono.getMonto())
                    .estado("CREADA")
                    .nombreCliente(nombreCliente)
                    .build());
            return true;
        } catch (Exception e) {
            log.warn("No se pudo registrar pendiente electrónica abono CxC #{}: {}",
                    abono != null ? abono.getId() : null, e.getMessage());
            return false;
        }
    }

    /**
     * 1) sesionId del request (caja actual en FE)
     * 2) sesión activa del usuario
     * 3) sesion_id del recibo CxC (legacy; puede ser caja ya cerrada)
     */
    private Long resolverSesionParaPendiente(CuentaPorCobrar cxc, Long sesionIdCaja) {
        if (sesionIdCaja != null) {
            return sesionIdCaja;
        }
        UUID userId = SecurityContextHelper.getUserId();
        if (userId != null) {
            List<Sesion> activas = sesionRepository.findByUserIdAndEsActivoTrue(userId);
            if (activas != null && !activas.isEmpty()) {
                return activas.get(0).getId();
            }
        }
        if (cxc.getReciboId() != null) {
            return reciboRepository.findById(cxc.getReciboId())
                    .map(Recibo::getSesionId)
                    .orElse(null);
        }
        return null;
    }

    private static boolean esMetodoQrElectronico(MetodoPago mp) {
        String sigla = mp.getSigla() != null ? mp.getSigla().trim().toUpperCase() : "";
        String desc = mp.getDescripcion() != null ? mp.getDescripcion().toUpperCase() : "";
        return "QR".equals(sigla) || desc.contains("BANCOLOMBIA");
    }

    /**
     * Opción B: saldo 0 → archivar ticket a historial+VTA con pagos = abonos;
     * cierra el ticket vivo para que una compra nueva abra ticket nuevo.
     */
    private void formalizarTicketSiLiquidada(CuentaPorCobrar cxc) {
        if (cxc.getReciboId() == null) {
            cuentaPorCobrarRepository.save(cxc);
            log.warn("CxC #{} PAGADA sin reciboId; no se formaliza ticket", cxc.getId());
            return;
        }

        List<AbonoCxc> abonos = abonoCxcRepository
                .findByCuentaPorCobrarIdOrderByFechaAbonoAscIdAsc(cxc.getId());
        List<ReciboPagoLineaDto> lineas = new ArrayList<>();
        for (AbonoCxc a : abonos) {
            if (a.getMonto() == null || a.getMetodoPagoId() == null) {
                continue;
            }
            lineas.add(ReciboPagoLineaDto.builder()
                    .metodoPagoId(a.getMetodoPagoId())
                    .monto(a.getMonto().setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        Long reciboId = cxc.getReciboId();
        Long ticketId = cxc.getTicketId();

        ReciboPagoResponseDto pago = reciboService.liquidarComoVentaDesdeCxc(reciboId, lineas);

        cxc.setHistorialReciboId(pago.getHistorialReciboId());
        cxc.setDocumentoVentaId(pago.getDocumentoVentaId());
        cuentaPorCobrarRepository.saveAndFlush(cxc);
        cuentaPorCobrarRepository.detachReciboYTicket(cxc.getId());
        cxc.setReciboId(null);
        cxc.setTicketId(null);

        // Solo desvincular de UI; no borrar ticket/recibo (FK). liquidar ya quitó ticket_recibo.
        if (ticketId != null) {
            Optional<TicketRecibo> tr = ticketReciboRepository.findFirstByTicketId(ticketId);
            if (tr.isPresent()) {
                ticketReciboRepository.delete(tr.get());
                ticketReciboRepository.flush();
            }
        }
        if (reciboId != null) {
            Optional<TicketRecibo> trR = ticketReciboRepository.findFirstByReciboId(reciboId);
            if (trR.isPresent()) {
                ticketReciboRepository.delete(trR.get());
                ticketReciboRepository.flush();
            }
        }

        log.info(
                "CxC #{} liquidada → historial={} docVenta={} ticketDesvinculado={}",
                cxc.getId(), pago.getHistorialReciboId(), pago.getDocumentoVentaId(), ticketId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonoCxcDto> listarAbonos(Long cuentaId) {
        if (cuentaId == null) {
            return List.of();
        }
        return abonoCxcRepository.findByCuentaPorCobrarIdOrderByFechaAbonoDescIdDesc(cuentaId)
                .stream()
                .map(this::toAbonoDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CuentaPorCobrarDto anular(Long cuentaId, CerrarCuentaPorCobrarRequest request) {
        CuentaPorCobrar cxc = requireVigente(cuentaId);
        long abonos = abonoCxcRepository.countByCuentaPorCobrarId(cxc.getId());
        if (abonos > 0) {
            throw new IllegalArgumentException(
                    "No se puede anular: la cuenta #" + cxc.getId()
                            + " ya tiene " + abonos + " abono(s). Use castigo de cartera o cobre el saldo.");
        }

        MotivoOperacion motivo = resolverMotivo(
                request != null ? request.getMotivoOperacionId() : null,
                MOTIVO_ANULAR);
        String texto = request != null ? trimToNull(request.getMotivoTexto()) : null;
        if (texto == null) {
            texto = motivo.getNombre();
        }

        aplicarCierreTraza(cxc, "ANULADA", motivo, texto, null, null);
        // Ticket sigue vivo: el cajero puede cobrar de contado.
        log.info("CxC #{} ANULADA (0 abonos). Ticket {} sigue disponible.", cxc.getId(), cxc.getTicketId());
        return toDto(cxc);
    }

    @Override
    @Transactional
    public CuentaPorCobrarDto castigar(Long cuentaId, CerrarCuentaPorCobrarRequest request) {
        CuentaPorCobrar cxc = requireVigente(cuentaId);
        String texto = request != null ? trimToNull(request.getMotivoTexto()) : null;
        if (texto == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el motivo del castigo de cartera (traza obligatoria).");
        }
        MotivoOperacion motivo = resolverMotivo(
                request.getMotivoOperacionId(),
                MOTIVO_CASTIGO);

        Long reciboId = cxc.getReciboId();
        Long ticketId = cxc.getTicketId();
        Long movInvId = null;
        BigDecimal valorCosto = BigDecimal.ZERO;

        if (reciboId != null) {
            List<ReciboDetalle> detalles = reciboDetalleService.findEntityListByReciboId(reciboId);
            try {
                CastigoCarteraInventarioResult inv = movimientoInventarioService.registrarCastigoCartera(
                        reciboId,
                        cxc.getId(),
                        detalles,
                        SecurityContextHelper.getUserId(),
                        "Castigo CxC #" + cxc.getId() + " · " + texto);
                if (inv != null) {
                    movInvId = inv.getMovimientoInventarioId();
                    valorCosto = inv.getValorPerdidaCosto() != null
                            ? inv.getValorPerdidaCosto()
                            : BigDecimal.ZERO;
                }
            } catch (Exception e) {
                log.warn("CxC #{} castigo: fallo inventario (¿SQL 43 / CASTIGO_CARTERA?): {}",
                        cxc.getId(), e.getMessage());
                throw new IllegalStateException(
                        "No se pudo registrar la salida de inventario del castigo: " + e.getMessage(), e);
            }
        }

        aplicarCierreTraza(cxc, "CASTIGADA", motivo, texto, movInvId, valorCosto);

        // Suelta FKs en BD. No borramos recibo ni ticket (FK); solo el vínculo ticket_recibo
        // para que el ticket deje de listarse en POS.
        int detached = cuentaPorCobrarRepository.detachReciboYTicket(cxc.getId());
        if (detached < 1) {
            log.warn("CxC #{} castigo: detachReciboYTicket no actualizó filas", cxc.getId());
        }
        cxc.setReciboId(null);
        cxc.setTicketId(null);
        desvincularTicketTrasCastigo(reciboId, ticketId);

        // Recargar estado limpio tras clearAutomatically del @Modifying
        CuentaPorCobrar refreshed = cuentaPorCobrarRepository.findById(cxc.getId()).orElse(cxc);
        log.info(
                "CxC #{} CASTIGADA movInv={} costo={} saldoCastigado={}",
                refreshed.getId(), movInvId, valorCosto, refreshed.getSaldoPendiente());
        return toDto(refreshed);
    }

    private CuentaPorCobrar requireVigente(Long cuentaId) {
        if (cuentaId == null) {
            throw new IllegalArgumentException("Debe indicar la cuenta por cobrar.");
        }
        CuentaPorCobrar cxc = cuentaPorCobrarRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cuenta por cobrar no encontrada: " + cuentaId));
        if (!ESTADOS_VIGENTES.contains(cxc.getEstado())) {
            throw new IllegalArgumentException(
                    "La cuenta #" + cuentaId + " no está vigente (estado " + cxc.getEstado() + ").");
        }
        return cxc;
    }

    private MotivoOperacion resolverMotivo(Long motivoId, String codigoDefault) {
        if (motivoId != null) {
            return motivoOperacionRepository.findById(motivoId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Motivo de operación no encontrado: " + motivoId));
        }
        return motivoOperacionRepository.findByCodigoAndActivoTrue(codigoDefault)
                .orElseThrow(() -> new IllegalStateException(
                        "Motivo " + codigoDefault + " no configurado. Ejecute SQL 43_cxc_anular_castigar.sql."));
    }

    private void aplicarCierreTraza(
            CuentaPorCobrar cxc,
            String estado,
            MotivoOperacion motivo,
            String texto,
            Long movimientoInventarioId,
            BigDecimal valorPerdidaCosto
    ) {
        UUID userId = SecurityContextHelper.getUserId();
        cxc.setEstado(estado);
        cxc.setFechaCierre(DateUtils.obtenerFechaSistema());
        cxc.setUsuarioCierreId(userId);
        cxc.setMotivoOperacionId(motivo != null ? motivo.getId() : null);
        cxc.setMotivoCierreTexto(texto);
        if (movimientoInventarioId != null) {
            cxc.setMovimientoInventarioId(movimientoInventarioId);
        }
        if (valorPerdidaCosto != null) {
            cxc.setValorPerdidaCosto(valorPerdidaCosto);
        }
        String obs = trimToNull(cxc.getObservacion());
        String marca = estado + " · " + texto;
        cxc.setObservacion(obs == null ? marca : obs + " · " + marca);
        cuentaPorCobrarRepository.save(cxc);
    }

    /**
     * Quita el ticket de la UI (borra ticket_recibo). No elimina filas ticket/recibo
     * para no pelear con FK {@code fk_cxc_ticket} / {@code fk_cxc_recibo}.
     */
    private void desvincularTicketTrasCastigo(Long reciboId, Long ticketId) {
        if (reciboId != null) {
            Optional<TicketRecibo> tr = ticketReciboRepository.findFirstByReciboId(reciboId);
            if (tr.isPresent()) {
                ticketReciboRepository.delete(tr.get());
                ticketReciboRepository.flush();
            }
        }
        if (ticketId != null) {
            Optional<TicketRecibo> trTicket = ticketReciboRepository.findFirstByTicketId(ticketId);
            if (trTicket.isPresent()) {
                ticketReciboRepository.delete(trTicket.get());
                ticketReciboRepository.flush();
            }
        }
    }

    private AbonoCxcDto toAbonoDto(AbonoCxc entity) {
        MetodoPago mp = entity.getMetodoPagoId() != null
                ? metodoPagoRepository.findById(entity.getMetodoPagoId()).orElse(null)
                : null;
        boolean pendiente = entity.getId() != null
                && historialReciboElectronicoRepository.findByAbonoCxcId(entity.getId()).isPresent();
        return toAbonoDto(entity, mp, pendiente);
    }

    private AbonoCxcDto toAbonoDto(AbonoCxc entity, MetodoPago mp, boolean requiereConfirmacionElectronica) {
        return AbonoCxcDto.builder()
                .id(entity.getId())
                .cuentaPorCobrarId(entity.getCuentaPorCobrarId())
                .fechaAbono(entity.getFechaAbono())
                .monto(entity.getMonto())
                .metodoPagoId(entity.getMetodoPagoId())
                .metodoPagoDescripcion(mp != null ? mp.getDescripcion() : null)
                .origenFondosId(entity.getOrigenFondosId())
                .movimientoOrigenFondosId(entity.getMovimientoOrigenFondosId())
                .observacion(entity.getObservacion())
                .clientePagadorId(entity.getClientePagadorId())
                .clientePagadorNombre(entity.getClientePagadorNombre())
                .requiereConfirmacionElectronica(requiereConfirmacionElectronica)
                .build();
    }

    private CuentaPorCobrarDto toDto(CuentaPorCobrar entity) {
        Client cliente = entity.getClienteId() != null
                ? clientRepository.findById(entity.getClienteId()).orElse(null)
                : null;
        return toDto(entity, cliente);
    }

    private CuentaPorCobrarDto toDto(CuentaPorCobrar entity, Client cliente) {
        long abonos = entity.getId() != null
                ? abonoCxcRepository.countByCuentaPorCobrarId(entity.getId())
                : 0L;
        return CuentaPorCobrarDto.builder()
                .id(entity.getId())
                .historialReciboId(entity.getHistorialReciboId())
                .documentoVentaId(entity.getDocumentoVentaId())
                .reciboId(entity.getReciboId())
                .ticketId(entity.getTicketId())
                .clienteId(entity.getClienteId())
                .clienteNombre(cliente != null ? cliente.getNombre() : null)
                .clienteTelefono(cliente != null ? cliente.getTelefono() : null)
                .clienteCorreo(cliente != null ? cliente.getCorreo() : null)
                .fechaOrigen(entity.getFechaOrigen())
                .montoOriginal(entity.getMontoOriginal())
                .saldoPendiente(entity.getSaldoPendiente())
                .totalTicket(entity.getTotalTicket())
                .estado(entity.getEstado())
                .observacion(entity.getObservacion())
                .fechaCierre(entity.getFechaCierre())
                .motivoCierreTexto(entity.getMotivoCierreTexto())
                .movimientoInventarioId(entity.getMovimientoInventarioId())
                .valorPerdidaCosto(entity.getValorPerdidaCosto())
                .cantidadAbonos(abonos)
                .build();
    }

    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }
}
