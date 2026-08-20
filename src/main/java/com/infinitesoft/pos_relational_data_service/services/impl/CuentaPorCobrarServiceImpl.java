package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.AbonoCxcDto;
import com.infinitesoft.pos_relational_data_service.dto.AbrirCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CuentaPorCobrarDto;
import com.infinitesoft.pos_relational_data_service.dto.MovimientoOrigenFondosDto;
import com.infinitesoft.pos_relational_data_service.dto.RegistrarAbonoCxcRequest;
import com.infinitesoft.pos_relational_data_service.entities.AbonoCxc;
import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.entities.CuentaPorCobrar;
import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;
import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.repositories.AbonoCxcRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ClientRepository;
import com.infinitesoft.pos_relational_data_service.repositories.CuentaPorCobrarRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.OrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.CuentaPorCobrarService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CuentaPorCobrarServiceImpl implements CuentaPorCobrarService {

    private static final List<String> ESTADOS_VIGENTES = Arrays.asList("ABIERTA", "PARCIAL");
    private static final List<String> ESTADOS_ARCHIVADOS = Arrays.asList("PAGADA", "ANULADA");

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
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private OrigenFondosRepository origenFondosRepository;

    @Autowired
    private MovimientoOrigenFondosService movimientoOrigenFondosService;

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
                .estado("ABIERTA")
                .observacion(buildObservacionApertura(trimToNull(request.getObservacion()), totalTicket, abono, saldoCredito))
                .usuarioId(SecurityContextHelper.getUserId())
                .build();

        CuentaPorCobrar saved = cuentaPorCobrarRepository.save(entity);
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

        MetodoPago mp = metodoPagoRepository.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Método de pago no encontrado: " + request.getMetodoPagoId()));

        OrigenFondos origen;
        if (request.getOrigenFondosId() != null) {
            origen = origenFondosRepository.findById(request.getOrigenFondosId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Origen de fondos no encontrado: " + request.getOrigenFondosId()));
        } else {
            origen = origenFondosRepository.findByMetodoPagoId(mp.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No hay origen de fondos raíz para el medio «"
                                    + mp.getDescripcion() + "»."));
        }

        Client cliente = cxc.getClienteId() != null
                ? clientRepository.findById(cxc.getClienteId()).orElse(null)
                : null;
        String tercero = cliente != null ? cliente.getNombre() : null;
        String obs = trimToNull(request.getObservacion());
        if (obs == null) {
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

        BigDecimal nuevoSaldo = saldo.subtract(monto);
        cxc.setSaldoPendiente(nuevoSaldo);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            cxc.setEstado("PAGADA");
        } else {
            cxc.setEstado("PARCIAL");
        }
        cuentaPorCobrarRepository.save(cxc);

        log.info(
                "Abono CxC id={} cuenta={} monto={} saldoNuevo={} estado={} movOF={}",
                savedAbono.getId(), cxc.getId(), monto, nuevoSaldo, cxc.getEstado(),
                mov.getId());
        return toAbonoDto(savedAbono, mp);
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

    private AbonoCxcDto toAbonoDto(AbonoCxc entity) {
        MetodoPago mp = entity.getMetodoPagoId() != null
                ? metodoPagoRepository.findById(entity.getMetodoPagoId()).orElse(null)
                : null;
        return toAbonoDto(entity, mp);
    }

    private AbonoCxcDto toAbonoDto(AbonoCxc entity, MetodoPago mp) {
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
                .build();
    }

    private CuentaPorCobrarDto toDto(CuentaPorCobrar entity) {
        Client cliente = entity.getClienteId() != null
                ? clientRepository.findById(entity.getClienteId()).orElse(null)
                : null;
        return toDto(entity, cliente);
    }

    private CuentaPorCobrarDto toDto(CuentaPorCobrar entity, Client cliente) {
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
                .estado(entity.getEstado())
                .observacion(entity.getObservacion())
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
