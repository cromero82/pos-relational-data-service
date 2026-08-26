package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.*;
import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.CorteVentaDetalle;
import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.entities.Establecimiento;
import com.infinitesoft.pos_relational_data_service.entities.MovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.Persona;
import com.infinitesoft.pos_relational_data_service.entities.Proveedor;
import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaEgreso;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoMovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.repositories.OrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MotivoMovimientoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MovimientoOrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.PersonaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProveedorRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.EstablecimientoService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovimientoOrigenFondosServiceImpl implements MovimientoOrigenFondosService {

    @Autowired
    private MovimientoOrigenFondosRepository movimientoRepository;

    @Autowired
    private OrigenFondosRepository cuentaRepository;

    @Autowired
    private MotivoMovimientoRepository motivoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EstablecimientoService establecimientoService;

    @Override
    public BigDecimal calcularSaldo(Integer origenFondosId) {
        BigDecimal saldo = movimientoRepository.sumImpactoByCuentaId(origenFondosId);
        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    @Override
    public List<MovimientoOrigenFondosDto> findByOrigen(Integer origenFondosId) {
        return movimientoRepository.findByOrigenFondosIdOrderByFechaCreacionDescIdDesc(origenFondosId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoOrigenFondosDto> findByGrupoTrasladoId(String grupoTrasladoId) {
        if (grupoTrasladoId == null || grupoTrasladoId.isBlank()) {
            throw new IllegalArgumentException("grupoTrasladoId es obligatorio.");
        }
        List<MovimientoOrigenFondosDto> patas = movimientoRepository
                .findByGrupoTrasladoIdOrderByIdAsc(grupoTrasladoId.trim())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        if (patas.isEmpty()) {
            throw new EntityNotFoundException(
                    "No hay movimientos para grupoTrasladoId=" + grupoTrasladoId.trim());
        }
        return patas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoOrigenFondosDto> findCandidatosFormalizarEgreso(
            List<Integer> origenFondosIds,
            BigDecimal valor
    ) {
        if (origenFondosIds == null || origenFondosIds.isEmpty()) {
            return List.of();
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a cero.");
        }
        return movimientoRepository.findCandidatosFormalizarEgreso(origenFondosIds, valor)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoOrigenFondosDto> findPorClasificacion(
            String clasificacionOperativa,
            LocalDate desde,
            LocalDate hasta
    ) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("hasta no puede ser anterior a desde");
        }
        String clasif = null;
        if (clasificacionOperativa != null && !clasificacionOperativa.isBlank()) {
            clasif = normalizarClasificacionOperativa(clasificacionOperativa);
        }
        return movimientoRepository
                .findPorClasificacionOperativa(clasif, desde, hasta)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public static final String ORIGEN_TIPO_BASE_INICIAL = "BASE_INICIAL";
    public static final String CODIGO_MOTIVO_INVERSION_INICIAL = "INVERSION_INICIAL_BASE";

    @Override
    @Transactional
    public MovimientoOrigenFondosDto registrarEntradaManual(MovimientoEntradaRequest request) {
        validarValorPositivo(request.getValor());
        if (request.getMotivoMovimientoId() != null) {
            motivoRepository.findById(request.getMotivoMovimientoId()).ifPresent(m -> {
                if (CODIGO_MOTIVO_INVERSION_INICIAL.equals(m.getCodigo())) {
                    throw new IllegalArgumentException(
                            "El motivo de inversión inicial solo puede usarse en el flujo de Base inicial.");
                }
            });
        }
        OrigenFondos cuenta = requireOrigen(request.getOrigenFondosId());
        LocalDate fecha = request.getFecha() != null ? request.getFecha() : DateUtils.obtenerFechaSistema().toLocalDate();
        MovimientoOrigenFondos saved = persistirMovimiento(
                cuenta,
                null,
                TipoMovimientoOrigenFondos.ENTRADA_MANUAL,
                request.getValor(),
                request.getValor(),
                fecha,
                null,
                request.getMotivoMovimientoId(),
                request.getObservacion(),
                null,
                null,
                "MANUAL",
                null,
                null
        );
        return toDto(saved);
    }

    public static final String ORIGEN_TIPO_ABONO_CXC = "ABONO_CXC";

    @Override
    @Transactional
    public MovimientoOrigenFondosDto registrarEntradaCobranza(
            Integer origenFondosId,
            BigDecimal monto,
            Long abonoCxcId,
            String terceroNombre,
            String observacion
    ) {
        validarValorPositivo(monto);
        OrigenFondos cuenta = requireOrigen(origenFondosId);
        LocalDate fecha = DateUtils.obtenerFechaSistema().toLocalDate();
        MovimientoOrigenFondos saved = persistirMovimiento(
                cuenta,
                null,
                TipoMovimientoOrigenFondos.ENTRADA_COBRANZA,
                monto,
                monto,
                fecha,
                terceroNombre,
                null,
                observacion,
                null,
                null,
                ORIGEN_TIPO_ABONO_CXC,
                abonoCxcId,
                null
        );
        return toDto(saved);
    }

    /**
     * Entrada de instalación: define la Base del primer corte (origenTipo BASE_INICIAL).
     */
    @Override
    @Transactional
    public MovimientoOrigenFondosDto registrarBaseInicial(BaseInicialRequest request) {
        validarValorPositivo(request.getValor());
        if (movimientoRepository.existsByOrigenTipo(ORIGEN_TIPO_BASE_INICIAL)) {
            throw new IllegalArgumentException("La base inicial ya fue registrada.");
        }
        OrigenFondos caja = cuentaRepository.findByActivoTrueOrderByOrdenAscIdAsc().stream()
                .filter(o -> o.getNombre() != null
                        && ("Caja: Efectivo".equalsIgnoreCase(o.getNombre().trim())
                        || "Caja Efectivo".equalsIgnoreCase(o.getNombre().trim())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el origen de fondos Caja: Efectivo."));
        var motivo = motivoRepository.findByCodigo(CODIGO_MOTIVO_INVERSION_INICIAL)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Motivo INVERSION_INICIAL_BASE no configurado. Ejecute 24_base_inicial_caja.sql."));
        LocalDate fecha = request.getFecha() != null ? request.getFecha() : DateUtils.obtenerFechaSistema().toLocalDate();
        MovimientoOrigenFondos saved = persistirMovimiento(
                caja,
                null,
                TipoMovimientoOrigenFondos.ENTRADA_MANUAL,
                request.getValor(),
                request.getValor(),
                fecha,
                null,
                motivo.getId(),
                request.getObservacion(),
                null,
                null,
                ORIGEN_TIPO_BASE_INICIAL,
                null,
                null
        );
        return toDto(saved);
    }

    @Override
    @Transactional
    public MovimientoOrigenFondosDto registrarPrestamo(MovimientoPrestamoRequest request) {
        validarValorPositivo(request.getValor());
        OrigenFondos cuenta = requireOrigen(request.getOrigenFondosId());
        LocalDate fecha = request.getFecha() != null ? request.getFecha() : DateUtils.obtenerFechaSistema().toLocalDate();
        MovimientoOrigenFondos saved = persistirMovimiento(
                cuenta,
                null,
                TipoMovimientoOrigenFondos.ENTRADA_PRESTAMO,
                request.getValor(),
                request.getValor(),
                fecha,
                request.getTerceroNombre(),
                null,
                request.getObservacion(),
                null,
                null,
                "PRESTAMO",
                null,
                null
        );
        return toDto(saved);
    }

    @Override
    @Transactional
    public List<MovimientoOrigenFondosDto> registrarTraslado(MovimientoTrasladoRequest request) {
        validarValorPositivo(request.getValor());
        if (request.getOrigenFondosId() == null || request.getOrigenDestinoId() == null) {
            throw new IllegalArgumentException("Origen y destino del traslado son obligatorios.");
        }
        if (request.getOrigenFondosId().equals(request.getOrigenDestinoId())) {
            throw new IllegalArgumentException("Origen y destino del traslado deben ser distintos.");
        }
        OrigenFondos origen = requireOrigen(request.getOrigenFondosId());
        OrigenFondos destino = requireOrigen(request.getOrigenDestinoId());
        validarSalidaSuficiente(origen.getId(), request.getValor());

        LocalDate fecha = request.getFecha() != null ? request.getFecha() : DateUtils.obtenerFechaSistema().toLocalDate();
        String grupoId = UUID.randomUUID().toString();
        String clasificacion = normalizarClasificacionOperativa(request.getClasificacionOperativa());

        MovimientoOrigenFondos salida = persistirMovimiento(
                origen,
                destino.getId(),
                TipoMovimientoOrigenFondos.TRASLADO,
                request.getValor(),
                request.getValor().negate(),
                fecha,
                null,
                null,
                request.getObservacion(),
                null,
                null,
                "TRASLADO",
                null,
                grupoId,
                clasificacion
        );

        MovimientoOrigenFondos entrada = persistirMovimiento(
                destino,
                null,
                TipoMovimientoOrigenFondos.TRASLADO,
                request.getValor(),
                request.getValor(),
                fecha,
                null,
                null,
                request.getObservacion(),
                null,
                null,
                "TRASLADO",
                null,
                grupoId,
                clasificacion
        );

        List<MovimientoOrigenFondosDto> resultado = new ArrayList<>();
        resultado.add(toDto(salida));
        resultado.add(toDto(entrada));
        return resultado;
    }

    @Override
    @Transactional
    public List<MovimientoOrigenFondosDto> registrarTrasladoDistribucion(
            Integer origenFondosId,
            Integer origenDestinoId,
            BigDecimal valor,
            Long corteVentaId,
            String observacion
    ) {
        validarValorPositivo(valor);
        if (origenFondosId == null || origenDestinoId == null) {
            throw new IllegalArgumentException("Origen y destino del traslado son obligatorios.");
        }
        if (origenFondosId.equals(origenDestinoId)) {
            throw new IllegalArgumentException("Origen y destino del traslado deben ser distintos.");
        }
        if (corteVentaId == null) {
            throw new IllegalArgumentException("corteVentaId es obligatorio para distribución.");
        }
        OrigenFondos origen = requireOrigen(origenFondosId);
        OrigenFondos destino = requireOrigen(origenDestinoId);
        validarSalidaSuficiente(origen.getId(), valor);

        LocalDate fecha = DateUtils.obtenerFechaSistema().toLocalDate();
        String grupoId = UUID.randomUUID().toString();
        String obs = observacion != null && !observacion.isBlank()
                ? observacion
                : "Distribución de efectivo corte #" + corteVentaId;

        MovimientoOrigenFondos salida = persistirMovimiento(
                origen,
                destino.getId(),
                TipoMovimientoOrigenFondos.TRASLADO,
                valor,
                valor.negate(),
                fecha,
                null,
                null,
                obs,
                null,
                null,
                "DISTRIBUCION",
                corteVentaId,
                grupoId
        );

        MovimientoOrigenFondos entrada = persistirMovimiento(
                destino,
                null,
                TipoMovimientoOrigenFondos.TRASLADO,
                valor,
                valor,
                fecha,
                null,
                null,
                obs,
                null,
                null,
                "DISTRIBUCION",
                corteVentaId,
                grupoId
        );

        List<MovimientoOrigenFondosDto> resultado = new ArrayList<>();
        resultado.add(toDto(salida));
        resultado.add(toDto(entrada));
        return resultado;
    }

    public static final String ORIGEN_TIPO_CORTE_VENTA = "CORTE_VENTA";
    public static final String ORIGEN_TIPO_CORTE_VENTA_REVERSO = "CORTE_VENTA_REVERSO";

    @Override
    @Transactional
    public void registrarEntradasVentaCorte(Long corteVentaId, List<CorteVentaDetalle> detalles) {
        if (corteVentaId == null || detalles == null || detalles.isEmpty()) {
            return;
        }
        if (!movimientoRepository
                .findByOrigenTipoAndIdReferenciaOrderByIdAsc(ORIGEN_TIPO_CORTE_VENTA, corteVentaId)
                .isEmpty()) {
            return;
        }
        LocalDate fecha = DateUtils.obtenerFechaSistema().toLocalDate();
        for (CorteVentaDetalle detalle : detalles) {
            if (detalle.getMetodoPagoId() == null) {
                continue;
            }
            BigDecimal ventas = detalle.getTotalVentasSistema();
            if (ventas == null || ventas.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            OrigenFondos cuenta = cuentaRepository.findByMetodoPagoId(detalle.getMetodoPagoId())
                    .orElse(null);
            if (cuenta == null) {
                continue;
            }
            persistirMovimiento(
                    cuenta,
                    null,
                    TipoMovimientoOrigenFondos.ENTRADA_VENTA,
                    ventas,
                    ventas,
                    fecha,
                    null,
                    null,
                    "Ventas del corte #" + corteVentaId,
                    ventas,
                    null,
                    ORIGEN_TIPO_CORTE_VENTA,
                    corteVentaId,
                    null
            );
        }
    }

    @Override
    @Transactional
    public void revertirEntradasVentaCorte(Long corteVentaId) {
        if (corteVentaId == null) {
            return;
        }
        if (!movimientoRepository
                .findByOrigenTipoAndIdReferenciaOrderByIdAsc(ORIGEN_TIPO_CORTE_VENTA_REVERSO, corteVentaId)
                .isEmpty()) {
            return;
        }
        List<MovimientoOrigenFondos> entradas = movimientoRepository
                .findByOrigenTipoAndIdReferenciaOrderByIdAsc(ORIGEN_TIPO_CORTE_VENTA, corteVentaId)
                .stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoOrigenFondos.ENTRADA_VENTA)
                .collect(Collectors.toList());
        for (MovimientoOrigenFondos entrada : entradas) {
            BigDecimal impacto = entrada.getImpacto().negate();
            BigDecimal saldoAntes = calcularSaldo(entrada.getOrigenFondosId());
            MovimientoOrigenFondos reverso = MovimientoOrigenFondos.builder()
                    .fecha(DateUtils.obtenerFechaSistema().toLocalDate())
                    .usuarioId(requireUsuarioId())
                    .origenFondosId(entrada.getOrigenFondosId())
                    .tipoMovimiento(TipoMovimientoOrigenFondos.REVERSO_ENTRADA_VENTA)
                    .valor(impacto.abs())
                    .impacto(impacto)
                    .saldoAntes(saldoAntes)
                    .saldoDespues(saldoAntes.add(impacto))
                    .metodoPagoId(entrada.getMetodoPagoId())
                    .observacion("Reverso ventas por eliminación del cierre #" + corteVentaId)
                    .valorSistema(entrada.getValorSistema())
                    .origenTipo(ORIGEN_TIPO_CORTE_VENTA_REVERSO)
                    .idReferencia(corteVentaId)
                    .build();
            movimientoRepository.save(reverso);
        }
    }

    @Override
    @Transactional
    public MovimientoOrigenFondosDto registrarAjuste(MovimientoAjusteRequest request) {
        OrigenFondos cuenta = requireOrigen(request.getOrigenFondosId());
        if (request.getValorSistema() == null || request.getValorReal() == null) {
            throw new IllegalArgumentException("Valor sistema y valor real son obligatorios para el ajuste.");
        }
        BigDecimal delta = request.getValorReal().subtract(request.getValorSistema());
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("El ajuste requiere diferencia entre valor real y valor sistema.");
        }
        LocalDate fecha = request.getFecha() != null ? request.getFecha() : DateUtils.obtenerFechaSistema().toLocalDate();
        BigDecimal valorAbs = delta.abs();
        MovimientoOrigenFondos saved = persistirMovimiento(
                cuenta,
                null,
                TipoMovimientoOrigenFondos.AJUSTE_SALDO,
                valorAbs,
                delta,
                fecha,
                null,
                request.getMotivoMovimientoId(),
                request.getObservacion(),
                request.getValorSistema(),
                request.getValorReal(),
                "AJUSTE",
                null,
                null
        );
        return toDto(saved);
    }

    @Override
    @Transactional
    public MovimientoOrigenFondosDto registrarAjusteCierre(
            Long metodoPagoId,
            BigDecimal totalSistema,
            BigDecimal totalReal,
            Integer motivoMovimientoId,
            Long corteVentaId,
            String observacion
    ) {
        if (metodoPagoId == null) {
            throw new IllegalArgumentException("metodoPagoId es obligatorio para AJUSTE_CIERRE.");
        }
        if (totalSistema == null || totalReal == null) {
            throw new IllegalArgumentException("totalSistema y totalReal son obligatorios.");
        }
        BigDecimal delta = totalReal.subtract(totalSistema);
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("AJUSTE_CIERRE requiere desfase distinto de cero.");
        }
        if (motivoMovimientoId == null) {
            throw new IllegalArgumentException("Debe indicar el motivo del desfase.");
        }
        OrigenFondos cuenta = cuentaRepository.findByMetodoPagoId(metodoPagoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay origen de fondos vinculado al medio de pago id=" + metodoPagoId));
        LocalDate fecha = DateUtils.obtenerFechaSistema().toLocalDate();
        String obs = observacion != null && !observacion.isBlank()
                ? observacion
                : "Ajuste por cierre de turno";
        MovimientoOrigenFondos saved = persistirMovimiento(
                cuenta,
                null,
                TipoMovimientoOrigenFondos.AJUSTE_CIERRE,
                delta.abs(),
                delta,
                fecha,
                null,
                motivoMovimientoId,
                obs,
                totalSistema,
                totalReal,
                "CIERRE",
                corteVentaId,
                null
        );
        return toDto(saved);
    }

    @Override
    @Transactional
    public void revertirAjustesCierre(Long corteVentaId) {
        if (corteVentaId == null) {
            return;
        }
        if (!movimientoRepository
                .findByOrigenTipoAndIdReferenciaOrderByIdAsc("CIERRE_REVERSO", corteVentaId)
                .isEmpty()) {
            return;
        }

        List<MovimientoOrigenFondos> ajustes = movimientoRepository
                .findByOrigenTipoAndIdReferenciaOrderByIdAsc("CIERRE", corteVentaId)
                .stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoOrigenFondos.AJUSTE_CIERRE)
                .collect(Collectors.toList());

        for (MovimientoOrigenFondos ajuste : ajustes) {
            BigDecimal impacto = ajuste.getImpacto().negate();
            BigDecimal saldoAntes = calcularSaldo(ajuste.getOrigenFondosId());
            MovimientoOrigenFondos reverso = MovimientoOrigenFondos.builder()
                    .fecha(DateUtils.obtenerFechaSistema().toLocalDate())
                    .usuarioId(requireUsuarioId())
                    .origenFondosId(ajuste.getOrigenFondosId())
                    .tipoMovimiento(TipoMovimientoOrigenFondos.REVERSO_AJUSTE_CIERRE)
                    .valor(impacto.abs())
                    .impacto(impacto)
                    .saldoAntes(saldoAntes)
                    .saldoDespues(saldoAntes.add(impacto))
                    .metodoPagoId(ajuste.getMetodoPagoId())
                    .motivoMovimientoId(ajuste.getMotivoMovimientoId())
                    .observacion("Reverso por eliminación del cierre #" + corteVentaId)
                    .valorSistema(ajuste.getValorReal())
                    .valorReal(ajuste.getValorSistema())
                    .origenTipo("CIERRE_REVERSO")
                    .idReferencia(corteVentaId)
                    .build();
            movimientoRepository.save(reverso);
        }
    }

    @Override
    @Transactional
    public MovimientoOrigenFondosDto registrarSalidaEgreso(Egreso egreso) {
        if (egreso == null || egreso.getId() == null) {
            throw new IllegalArgumentException("El egreso guardado es obligatorio para registrar el movimiento.");
        }
        if (egreso.getOrigenFondosId() == null) {
            throw new IllegalArgumentException("Debe indicar el origen de fondos del egreso.");
        }
        if (egreso.getValor() == null || egreso.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del egreso debe ser mayor a cero.");
        }
        OrigenFondos cuenta = requireOrigenParaSalidaEgreso(egreso);
        LocalDate fecha = egreso.getFecha() != null
                ? egreso.getFecha()
                : DateUtils.obtenerFechaSistema().toLocalDate();
        String observacion = buildObservacionEgreso(egreso);
        String clasificacion = egreso.getFromMovimientoOrigenFondosId() != null
                ? "GASTO_NEGOCIO"
                : null;
        MovimientoOrigenFondos saved = persistirMovimiento(
                cuenta,
                null,
                TipoMovimientoOrigenFondos.SALIDA_EGRESO,
                egreso.getValor(),
                egreso.getValor().negate(),
                fecha,
                null,
                null,
                observacion,
                null,
                null,
                "EGRESO",
                egreso.getId(),
                null,
                clasificacion
        );
        return toDto(saved);
    }

    @Override
    @Transactional
    public void revertirMovimientosEgreso(Long egresoId, String observacion) {
        if (egresoId == null) {
            return;
        }
        List<MovimientoOrigenFondos> salidas = movimientoRepository
                .findByOrigenTipoAndIdReferenciaOrderByIdAsc("EGRESO", egresoId)
                .stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoOrigenFondos.SALIDA_EGRESO)
                .collect(Collectors.toList());
        for (MovimientoOrigenFondos salida : salidas) {
            OrigenFondos cuenta = requireOrigen(salida.getOrigenFondosId());
            String obs = observacion != null && !observacion.isBlank()
                    ? observacion
                    : "Reversión egreso #" + egresoId;
            persistirMovimiento(
                    cuenta,
                    null,
                    TipoMovimientoOrigenFondos.ENTRADA_MANUAL,
                    salida.getValor(),
                    salida.getValor(),
                    salida.getFecha(),
                    null,
                    null,
                    obs,
                    null,
                    null,
                    "EGRESO_REVERSION",
                    egresoId,
                    null
            );
        }
    }

    @Override
    @Transactional
    public void sincronizarSalidaEgreso(Egreso anterior, Egreso actualizado) {
        if (actualizado == null || actualizado.getId() == null) {
            return;
        }
        boolean sinCambio = anterior != null
                && anterior.getOrigenFondosId() != null
                && anterior.getOrigenFondosId().equals(actualizado.getOrigenFondosId())
                && anterior.getValor() != null
                && anterior.getValor().compareTo(actualizado.getValor()) == 0
                && anterior.getFecha() != null
                && anterior.getFecha().equals(actualizado.getFecha());
        if (sinCambio) {
            return;
        }
        revertirMovimientosEgreso(actualizado.getId(), "Reversión por edición egreso #" + actualizado.getId());
        registrarSalidaEgreso(actualizado);
    }

    /**
     * Misma regla que EgresoServiceImpl: visibleEnEgreso, o Cuenta del dueño + persona dueño + PERSONAL/DIVIDENDOS.
     * La validación primaria está en egreso; aquí se refuerza para no abrir agujero en el ledger.
     */
    private OrigenFondos requireOrigenParaSalidaEgreso(Egreso egreso) {
        OrigenFondos cuenta = requireOrigen(egreso.getOrigenFondosId());
        if (Boolean.FALSE.equals(cuenta.getActivo())) {
            throw new IllegalArgumentException("La origen de fondos no está activa.");
        }
        if (Boolean.TRUE.equals(cuenta.getVisibleEnEgreso())) {
            return cuenta;
        }
        if (!esCuentaDelDueno(cuenta)) {
            throw new IllegalArgumentException("La origen de fondos no está habilitada para egresos.");
        }
        NaturalezaEgreso nat = egreso.getNaturaleza();
        if (nat != NaturalezaEgreso.PERSONAL && nat != NaturalezaEgreso.DIVIDENDOS) {
            throw new IllegalArgumentException(
                    "Cuenta del dueño solo aplica con naturaleza PERSONAL/DIVIDENDOS.");
        }
        Persona persona = egreso.getPersona();
        if (persona == null || persona.getId() == null) {
            throw new IllegalArgumentException(
                    "Cuenta del dueño requiere persona dueño/propietario.");
        }
        if (!Boolean.TRUE.equals(persona.getEsDuenoPropietario())) {
            // Puede venir solo {id}; resolver desde BD
            Persona loaded = personaRepository.findById(persona.getId()).orElse(null);
            if (loaded == null || !Boolean.TRUE.equals(loaded.getEsDuenoPropietario())) {
                throw new IllegalArgumentException(
                        "Cuenta del dueño solo aplica con persona marcada como dueño/propietario.");
            }
            egreso.setPersona(loaded);
        }
        return cuenta;
    }

    private boolean esCuentaDelDueno(OrigenFondos cuenta) {
        if (cuenta == null || cuenta.getParentOrigenFondosId() == null) {
            return false;
        }
        if (cuenta.getTipoOrigenFondos() != null
                && cuenta.getTipoOrigenFondos().getCodigo() != null
                && "DUENOS".equalsIgnoreCase(cuenta.getTipoOrigenFondos().getCodigo().trim())) {
            return true;
        }
        OrigenFondos padre = cuenta.getParentOrigen();
        if (padre == null) {
            padre = cuentaRepository.findById(cuenta.getParentOrigenFondosId()).orElse(null);
        }
        if (padre != null && padre.getTipoOrigenFondos() != null
                && padre.getTipoOrigenFondos().getCodigo() != null
                && "DUENOS".equalsIgnoreCase(padre.getTipoOrigenFondos().getCodigo().trim())) {
            return true;
        }
        String nombre = cuenta.getNombre() != null ? cuenta.getNombre().trim().toLowerCase() : "";
        return nombre.equals("cuenta del dueño")
                || nombre.equals("cuenta del dueno")
                || nombre.equals("personal administrador")
                || nombre.contains("cuenta del due");
    }

    private String buildObservacionEgreso(Egreso egreso) {
        // Formato: "Egreso #1: Beneficiario (observación opcional)"
        StringBuilder sb = new StringBuilder("Egreso");
        if (egreso.getId() != null) {
            sb.append(" #").append(egreso.getId());
        }
        String beneficiario = resolveBeneficiarioNombre(egreso);
        if (beneficiario != null && !beneficiario.isBlank()) {
            sb.append(": ").append(beneficiario.trim());
        }
        if (egreso.getDescripcion() != null && !egreso.getDescripcion().isBlank()) {
            sb.append(" (").append(egreso.getDescripcion().trim()).append(")");
        }
        return sb.toString();
    }

    /**
     * El FE suele enviar solo {@code proveedor/persona: { id }}, sin nombre.
     * Hay que resolver el nombre desde BD.
     */
    private String resolveBeneficiarioNombre(Egreso egreso) {
        if (egreso == null) {
            return null;
        }
        if (egreso.getPersona() != null) {
            Persona per = egreso.getPersona();
            if (per.getNombre() != null && !per.getNombre().isBlank()) {
                return per.getNombre();
            }
            if (per.getId() != null) {
                return personaRepository.findById(per.getId())
                        .map(Persona::getNombre)
                        .orElse(null);
            }
        }
        if (egreso.getProveedor() == null) {
            return null;
        }
        Proveedor p = egreso.getProveedor();
        if (p.getNombre() != null && !p.getNombre().isBlank()) {
            return p.getNombre();
        }
        if (p.getId() == null) {
            return null;
        }
        return proveedorRepository.findById(p.getId())
                .map(Proveedor::getNombre)
                .orElse(null);
    }

    private MovimientoOrigenFondos persistirMovimiento(
            OrigenFondos cuenta,
            Integer origenDestinoId,
            TipoMovimientoOrigenFondos tipo,
            BigDecimal valor,
            BigDecimal impacto,
            LocalDate fecha,
            String terceroNombre,
            Integer motivoMovimientoId,
            String observacion,
            BigDecimal valorSistema,
            BigDecimal valorReal,
            String origenTipo,
            Long idReferencia,
            String grupoTrasladoId
    ) {
        return persistirMovimiento(
                cuenta,
                origenDestinoId,
                tipo,
                valor,
                impacto,
                fecha,
                terceroNombre,
                motivoMovimientoId,
                observacion,
                valorSistema,
                valorReal,
                origenTipo,
                idReferencia,
                grupoTrasladoId,
                null
        );
    }

    private MovimientoOrigenFondos persistirMovimiento(
            OrigenFondos cuenta,
            Integer origenDestinoId,
            TipoMovimientoOrigenFondos tipo,
            BigDecimal valor,
            BigDecimal impacto,
            LocalDate fecha,
            String terceroNombre,
            Integer motivoMovimientoId,
            String observacion,
            BigDecimal valorSistema,
            BigDecimal valorReal,
            String origenTipo,
            Long idReferencia,
            String grupoTrasladoId,
            String clasificacionOperativa
    ) {
        if (impacto.compareTo(BigDecimal.ZERO) < 0) {
            validarSalidaSuficiente(cuenta.getId(), impacto.abs());
        }

        BigDecimal saldoAntes = calcularSaldo(cuenta.getId());
        BigDecimal saldoDespues = saldoAntes.add(impacto);

        String usuarioId = requireUsuarioId();
        MovimientoOrigenFondos entity = MovimientoOrigenFondos.builder()
                .fecha(fecha)
                .usuarioId(usuarioId)
                .origenFondosId(cuenta.getId())
                .origenDestinoId(origenDestinoId)
                .tipoMovimiento(tipo)
                .valor(valor)
                .impacto(impacto)
                .saldoAntes(saldoAntes)
                .saldoDespues(saldoDespues)
                .metodoPagoId(cuenta.getMetodoPagoId())
                .terceroNombre(terceroNombre)
                .motivoMovimientoId(motivoMovimientoId)
                .observacion(observacion)
                .valorSistema(valorSistema)
                .valorReal(valorReal)
                .origenTipo(origenTipo)
                .idReferencia(idReferencia)
                .grupoTrasladoId(grupoTrasladoId)
                .clasificacionOperativa(clasificacionOperativa)
                .build();
        return movimientoRepository.save(entity);
    }

    private static String normalizarClasificacionOperativa(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String c = raw.trim().toUpperCase();
        switch (c) {
            case "CUENTA_PERSONAL":
            case "ANTICIPO_SALARIO":
            case "VALE_EMPLEADO":
            case "GASTO_NEGOCIO":
            case "OTRO_LEGALIZADO":
                return c;
            default:
                throw new IllegalArgumentException(
                        "clasificacionOperativa inválida: " + raw
                                + " (use CUENTA_PERSONAL|ANTICIPO_SALARIO|VALE_EMPLEADO|GASTO_NEGOCIO|OTRO_LEGALIZADO)");
        }
    }

    private void validarSalidaSuficiente(Integer cuentaId, BigDecimal valor) {
        if (!isManejoEstricto()) {
            return;
        }
        BigDecimal saldo = calcularSaldo(cuentaId);
        if (saldo.compareTo(valor) < 0) {
            throw new IllegalStateException(
                    "Saldo insuficiente en la cuenta. Disponible: " + saldo + ", requerido: " + valor
                            + ". Registre un traslado o entrada antes de continuar.");
        }
    }

    private boolean isManejoEstricto() {
        Establecimiento establecimiento = establecimientoService.getActualEntity();
        return establecimiento != null && Boolean.TRUE.equals(establecimiento.getManejoEstrictoCuentas());
    }

    private OrigenFondos requireOrigen(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("La origen de fondos es obligatoria.");
        }
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Origen de fondos no encontrada: " + id));
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a cero.");
        }
    }

    private String requireUsuarioId() {
        if (SecurityContextHelper.getUserId() == null) {
            throw new IllegalStateException("Usuario no autenticado.");
        }
        return SecurityContextHelper.getUserId().toString();
    }

    private MovimientoOrigenFondosDto toDto(MovimientoOrigenFondos m) {
        String cuentaNombre = cuentaRepository.findById(m.getOrigenFondosId())
                .map(OrigenFondos::getNombre)
                .orElse(null);
        String destinoNombre = m.getOrigenDestinoId() != null
                ? cuentaRepository.findById(m.getOrigenDestinoId()).map(OrigenFondos::getNombre).orElse(null)
                : null;
        String motivoNombre = m.getMotivoMovimientoId() != null
                ? motivoRepository.findById(m.getMotivoMovimientoId()).map(mot -> mot.getNombre()).orElse(null)
                : null;
        return MovimientoOrigenFondosDto.builder()
                .id(m.getId())
                .fecha(m.getFecha())
                .usuarioId(m.getUsuarioId())
                .origenFondosId(m.getOrigenFondosId())
                .origenFondosNombre(cuentaNombre)
                .origenDestinoId(m.getOrigenDestinoId())
                .origenDestinoNombre(destinoNombre)
                .tipoMovimiento(m.getTipoMovimiento() != null ? m.getTipoMovimiento().name() : null)
                .valor(m.getValor())
                .impacto(m.getImpacto())
                .saldoAntes(m.getSaldoAntes())
                .saldoDespues(m.getSaldoDespues())
                .metodoPagoId(m.getMetodoPagoId())
                .terceroNombre(m.getTerceroNombre())
                .motivoMovimientoId(m.getMotivoMovimientoId())
                .motivoMovimientoNombre(motivoNombre)
                .observacion(m.getObservacion())
                .valorSistema(m.getValorSistema())
                .valorReal(m.getValorReal())
                .origenTipo(m.getOrigenTipo())
                .idReferencia(m.getIdReferencia())
                .origenId(m.getIdReferencia())
                .grupoTrasladoId(m.getGrupoTrasladoId())
                .clasificacionOperativa(m.getClasificacionOperativa())
                .periodoCierreId(m.getPeriodoCierreId())
                .build();
    }
}
