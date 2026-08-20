package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.BaseInicialPendienteDto;
import com.infinitesoft.pos_relational_data_service.dto.BaseInicialRequest;
import com.infinitesoft.pos_relational_data_service.dto.BaseInicialResultDto;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaDTO;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaDetalleDTO;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoRequest;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoResponse;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoPendienteDto;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoRequest;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoResultDto;
import com.infinitesoft.pos_relational_data_service.dto.FinalizarRevisionCorteRequest;
import com.infinitesoft.pos_relational_data_service.dto.MovimientoOrigenFondosDto;
import com.infinitesoft.pos_relational_data_service.dto.VentasTipoDTO;
import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;
import com.infinitesoft.pos_relational_data_service.entities.CorteVentaDetalle;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.MotivoMovimiento;
import com.infinitesoft.pos_relational_data_service.entities.MovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import com.infinitesoft.pos_relational_data_service.repositories.CorteVentaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.CorteVentaDetalleRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MotivoMovimientoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MovimientoOrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.OrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoMovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.CorteVentaService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CorteVentaServiceImpl implements CorteVentaService {

    @Autowired
    private CorteVentaRepository repository;

    @Autowired
    private CorteVentaDetalleRepository detalleRepository;

    @Autowired
    private HistorialReciboRepository historialReciboRepository;

    @Autowired
    private HistorialReciboPagoRepository historialReciboPagoRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private MovimientoOrigenFondosRepository movimientoOrigenFondosRepository;

    @Autowired
    private OrigenFondosRepository origenFondosRepository;

    @Autowired
    private MovimientoOrigenFondosService movimientoOrigenFondosService;

    @Autowired
    private MotivoMovimientoRepository motivoMovimientoRepository;

    @Override
    public CorteVenta create(CorteVenta corteVenta) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: create - fechaIni: {} - fechaFin: {}", corteVenta.getFechaIni(), corteVenta.getFechaFin());
        return repository.save(corteVenta);
    }

    @Override
    @Transactional
    public CorteVenta createFromDTO(CorteVentaDTO dto) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: createFromDTO - fechaIni: {} - fechaFin: {}", dto.getFechaIni(), dto.getFechaFin());
        validarMotivosDesfase(dto);
        if (dto.getObservacion() != null && dto.getObservacion().length() > 200) {
            throw new IllegalArgumentException("La observación no puede superar 200 caracteres.");
        }
        CorteVenta entity = convertToEntity(dto);
        boolean admin = isAdmin();

        // Obtener usuarioId del contexto de seguridad si no viene en el DTO o para asegurar el valor correcto
        if (SecurityContextHelper.getUserId() != null) {
            entity.setUsuarioId(SecurityContextHelper.getUserId().toString());
        }
        entity.setEstado(admin ? "revisada" : "creada");
        if (admin) {
            entity.setRevisadoPor(entity.getUsuarioId());
            entity.setFechaRevision(DateUtils.obtenerFechaSistema());
        }

        if (dto.isUltimoCorte() && dto.isActual()) {
            CorteVentaRangoRequest request = CorteVentaRangoRequest.builder()
                    .ultimoCorte(true)
                    .actual(true)
                    .build();
            CorteVentaRangoResponse rango = this.consultarRango(request);

            entity.setFechaIni(rango.getFechaIni());
            entity.setFechaFin(rango.getFechaFin());
            entity.setTotalSistema(rango.getTotal());

            if (dto.getVentasTipo() != null) {
                for (VentasTipo vt : entity.getVentasTipo()) {
                    rango.getVentasTipo().stream()
                            .filter(res -> res.getMetodoPagoId().equals(vt.getMetodoPagoId()))
                            .findFirst()
                            .ifPresent(res -> {
                                vt.setTotalVentasSistema(res.getTotalVentasSistema());
                                vt.setTotalEgresosSistema(res.getTotalEgresosSistema());
                                vt.setTotalSistema(res.getTotalSistema());
                                if (vt.getTotal() != null && vt.getTotalSistema() != null) {
                                    vt.setDesfase(vt.getTotal().subtract(vt.getTotalSistema()));
                                }
                            });
                }
            }
        }

        // Calcular ultimoHistorialReciboId si fechaIni y fechaFin están presentes
        if (entity.getFechaIni() != null && entity.getFechaFin() != null) {
            historialReciboRepository.findMaxIdByFechaCreacionBetween(entity.getFechaIni(), entity.getFechaFin())
                    .ifPresent(entity::setUltimoHistorialReciboId);
        }

        CorteVenta saved = repository.save(entity);
        List<CorteVentaDetalle> detalles = construirDetalles(dto, saved, admin);
        detalleRepository.saveAll(detalles);
        // Contabiliza ventas en ledger ANTES de ajustes de desfase y watermark.
        movimientoOrigenFondosService.registrarEntradasVentaCorte(saved.getId(), detalles);
        registrarAjustesCierre(detalles, saved.getId());
        // Watermark ledger DESPUÉS de ENTRADA_VENTA + AJUSTE_CIERRE (pertenecen a este corte).
        movimientoOrigenFondosRepository.findMaxId().ifPresent(maxId -> {
            saved.setUltimoMovimientoOrigenFondosId(maxId);
            saved.setDistribucionEfectivoEstado("PENDIENTE");
            repository.save(saved);
        });
        if (saved.getDistribucionEfectivoEstado() == null) {
            saved.setDistribucionEfectivoEstado("PENDIENTE");
            repository.save(saved);
        }
        return saved;
    }

    private void validarMotivosDesfase(CorteVentaDTO dto) {
        if (dto.getDetalles() != null && !dto.getDetalles().isEmpty()) {
            for (CorteVentaDetalleDTO d : dto.getDetalles()) {
                if ("SOLO_VISIBLE".equals(d.getModoCaptura())) {
                    continue;
                }
                validarMotivoDesfase(d.getMetodoPagoId(), d.getDesfase(), d.getTotal(), d.getTotalSistema(),
                        d.getMotivoDesfaseId());
            }
            return;
        }
        if (dto.getVentasTipo() == null) {
            return;
        }
        for (VentasTipoDTO vt : dto.getVentasTipo()) {
            validarMotivoDesfase(vt.getMetodoPagoId(), vt.getDesfase(), vt.getTotal(), vt.getTotalSistema(),
                    vt.getMotivoDesfaseId());
        }
    }

    private void validarMotivoDesfase(
            Long metodoPagoId,
            BigDecimal desfase,
            BigDecimal total,
            BigDecimal totalSistema,
            Integer motivoDesfaseId
    ) {
        BigDecimal delta = desfase;
        if (delta == null && total != null && totalSistema != null) {
            delta = total.subtract(totalSistema);
        }
        boolean hayDesfase = delta != null && delta.compareTo(BigDecimal.ZERO) != 0;
        if (hayDesfase && motivoDesfaseId == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el motivo del desfase para el medio de pago id=" + metodoPagoId);
        }
        if (!hayDesfase || motivoDesfaseId == null) {
            return;
        }
        MotivoMovimiento motivo = motivoMovimientoRepository.findById(motivoDesfaseId).orElse(null);
        if (motivo == null) {
            throw new IllegalArgumentException(
                    "Motivo de desfase inválido id=" + motivoDesfaseId);
        }
        String accion = motivo.getAccionEsperada() != null
                ? motivo.getAccionEsperada().trim().toUpperCase()
                : "";
        // REGISTRAR_DOCUMENTO = ir a documentar; no cierra el corte con AJUSTE_CIERRE.
        if ("REGISTRAR_DOCUMENTO".equals(accion)) {
            throw new IllegalArgumentException(
                    "El motivo «" + motivo.getNombre()
                            + "» exige registrar el egreso/movimiento faltante, "
                            + "volver a consultar el rango y, si aún hay diferencia, "
                            + "usar otro motivo (p. ej. error de conteo). Medio id="
                            + metodoPagoId);
        }
    }

    private List<CorteVentaDetalle> construirDetalles(CorteVentaDTO dto, CorteVenta corte, boolean admin) {
        List<CorteVentaDetalleDTO> entrada = dto.getDetalles();
        if (entrada == null || entrada.isEmpty()) {
            entrada = dto.getVentasTipo() == null ? Collections.emptyList() : dto.getVentasTipo().stream()
                    .map(vt -> CorteVentaDetalleDTO.builder()
                            .metodoPagoId(vt.getMetodoPagoId())
                            .totalVentasSistema(vt.getTotalVentasSistema())
                            .totalEgresosSistema(vt.getTotalEgresosSistema())
                            .totalSistema(vt.getTotalSistema())
                            .total(vt.getTotal())
                            .desfase(vt.getDesfase())
                            .motivoDesfaseId(vt.getMotivoDesfaseId())
                            .modoCaptura("DECLARADO_CAJERO")
                            .build())
                    .collect(Collectors.toList());
        }

        String usuarioId = corte.getUsuarioId();
        List<CorteVentaDetalle> resultado = new ArrayList<>();
        int orden = 0;
        for (CorteVentaDetalleDTO d : entrada) {
            String modo = admin ? "DECLARADO_ADMIN"
                    : ("SOLO_VISIBLE".equals(d.getModoCaptura()) ? "SOLO_VISIBLE" : "DECLARADO_CAJERO");
            BigDecimal base = nz(d.getBase());
            BigDecimal ventas = nz(d.getTotalVentasSistema());
            BigDecimal egresos = nz(d.getTotalEgresosSistema());
            BigDecimal movimientos = nz(d.getTotalMovimientosSistema());
            BigDecimal sistema = d.getTotalSistema() != null
                    ? d.getTotalSistema()
                    : base.add(ventas).subtract(egresos).add(movimientos);
            BigDecimal total = "SOLO_VISIBLE".equals(modo) ? null : d.getTotal();
            BigDecimal desfase = total != null ? total.subtract(sistema) : null;

            resultado.add(CorteVentaDetalle.builder()
                    .corteVentaId(corte.getId())
                    .metodoPagoId(d.getMetodoPagoId())
                    .origenFondosId(d.getOrigenFondosId())
                    .base(base)
                    .totalVentasSistema(ventas)
                    .totalEgresosSistema(egresos)
                    .totalMovimientosSistema(movimientos)
                    .totalSistema(sistema)
                    .total(total)
                    .desfase(desfase)
                    .motivoDesfaseId(d.getMotivoDesfaseId())
                    .modoCaptura(modo)
                    .declaradoPor("SOLO_VISIBLE".equals(modo) ? null : usuarioId)
                    .revisionEstado(admin ? "OK" : "PENDIENTE")
                    .ajusteGenerado(false)
                    .orden(d.getOrden() != null ? d.getOrden() : orden++)
                    .build());
        }
        return resultado;
    }

    private void registrarAjustesCierre(List<CorteVentaDetalle> detalles, Long corteId) {
        for (CorteVentaDetalle detalle : detalles) {
            if ("SOLO_VISIBLE".equals(detalle.getModoCaptura())) {
                continue;
            }
            registrarAjusteDetalle(detalle, corteId);
        }
        detalleRepository.saveAll(detalles);
    }

    private void registrarAjusteDetalle(CorteVentaDetalle detalle, Long corteId) {
        BigDecimal desfase = detalle.getDesfase();
        if (desfase == null || desfase.compareTo(BigDecimal.ZERO) == 0
                || Boolean.TRUE.equals(detalle.getAjusteGenerado())) {
            return;
        }
        if (detalle.getMotivoDesfaseId() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el motivo del desfase para el medio de pago id=" + detalle.getMetodoPagoId());
        }
        if (detalle.getMetodoPagoId() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio para generar AJUSTE_CIERRE.");
        }
            movimientoOrigenFondosService.registrarAjusteCierre(
                    detalle.getMetodoPagoId(),
                    nz(detalle.getTotalSistema()),
                    nz(detalle.getTotal()),
                    detalle.getMotivoDesfaseId(),
                    corteId,
                    "Ajuste por cierre #" + corteId
            );
        detalle.setAjusteGenerado(true);
    }

    @Override
    public List<CorteVenta> findAll() {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: findAll");
        return repository.findAll();
    }

    @Override
    public CorteVenta findById(Long id) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: findById para ID: {}", id);
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public CorteVenta update(Long id, CorteVenta corteVenta) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: update para ID: {} - fechaIni: {} - fechaFin: {}", id, corteVenta.getFechaIni(), corteVenta.getFechaFin());
        if (id == null) return null;
        Optional<CorteVenta> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        
        CorteVenta existing = existingOpt.get();
        existing.setUsuarioId(corteVenta.getUsuarioId());
        existing.setFechaCreacion(corteVenta.getFechaCreacion());
        existing.setFechaIni(corteVenta.getFechaIni());
        existing.setFechaFin(corteVenta.getFechaFin());
        existing.setUltimoHistorialReciboId(corteVenta.getUltimoHistorialReciboId());
        existing.setTotal(corteVenta.getTotal());
        existing.setTotalSistema(corteVenta.getTotalSistema());
        existing.setVentasTipo(corteVenta.getVentasTipo());
        
        return repository.save(existing);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: delete para ID: {}", id);
        if (id == null) return false;
        CorteVenta corte = repository.findById(id).orElse(null);
        if (corte == null) return false;
        if ("eliminado".equals(corte.getEstado())) {
            return true;
        }
        CorteVenta ultimo = repository.findFirstByEstadoNotOrderByIdDesc("eliminado")
                .orElseThrow(() -> new IllegalStateException("No existe un último corte vigente."));
        if (!ultimo.getId().equals(id)) {
            throw new IllegalStateException("Solo se puede eliminar el último corte de ventas vigente.");
        }
        movimientoOrigenFondosService.revertirAjustesCierre(id);
        movimientoOrigenFondosService.revertirEntradasVentaCorte(id);
        corte.setEstado("eliminado");
        repository.save(corte);
        return true;
    }

    @Override
    @Transactional
    public CorteVentaDTO finalizarRevision(Long id, FinalizarRevisionCorteRequest request) {
        if (!isAdmin()) {
            throw new IllegalStateException("Solo un administrador puede finalizar la revisión.");
        }
        CorteVenta corte = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corte no encontrado: " + id));
        if (!"creada".equals(corte.getEstado())) {
            throw new IllegalStateException("Solo los cortes en estado creada pueden revisarse.");
        }

        Map<Long, FinalizarRevisionCorteRequest.DetalleRevision> cambios =
                request != null && request.getDetalles() != null
                        ? request.getDetalles().stream().collect(Collectors.toMap(
                                FinalizarRevisionCorteRequest.DetalleRevision::getDetalleId,
                                x -> x,
                                (a, b) -> b))
                        : Collections.emptyMap();
        List<CorteVentaDetalle> detalles =
                detalleRepository.findByCorteVentaIdOrderByOrdenAscIdAsc(id);
        if (detalles.isEmpty()) {
            throw new IllegalStateException("El corte no contiene detalles para revisar.");
        }

        for (CorteVentaDetalle detalle : detalles) {
            FinalizarRevisionCorteRequest.DetalleRevision cambio = cambios.get(detalle.getId());
            if (cambio == null) {
                throw new IllegalArgumentException("Falta revisar el detalle id=" + detalle.getId());
            }
            validarRevision(cambio);

            if ("SOLO_VISIBLE".equals(detalle.getModoCaptura())) {
                if (cambio.getTotal() == null || cambio.getTotal().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException(
                            "Debe declarar el total real del detalle id=" + detalle.getId());
                }
                detalle.setTotal(cambio.getTotal());
                detalle.setDesfase(cambio.getTotal().subtract(nz(detalle.getTotalSistema())));
                detalle.setMotivoDesfaseId(cambio.getMotivoDesfaseId());
                detalle.setDeclaradoPor(SecurityContextHelper.getUserId().toString());
                validarMotivoDesfase(detalle.getMetodoPagoId(), detalle.getDesfase(), detalle.getTotal(),
                        detalle.getTotalSistema(), detalle.getMotivoDesfaseId());
                registrarAjusteDetalle(detalle, corte.getId());
            } else {
                if ((cambio.getTotal() != null && !sameMoney(cambio.getTotal(), detalle.getTotal()))
                        || (cambio.getMotivoDesfaseId() != null
                        && !cambio.getMotivoDesfaseId().equals(detalle.getMotivoDesfaseId()))) {
                    throw new IllegalStateException(
                            "Los montos declarados por el cajero no pueden modificarse: detalle id="
                                    + detalle.getId());
                }
            }

            detalle.setRevisionEstado(cambio.getRevisionEstado().toUpperCase());
            detalle.setRevisionComentario("SUGERENCIA".equalsIgnoreCase(cambio.getRevisionEstado())
                    ? cambio.getRevisionComentario().trim()
                    : null);
        }

        detalleRepository.saveAll(detalles);
        corte.setEstado("revisada");
        corte.setRevisadoPor(SecurityContextHelper.getUserId().toString());
        corte.setFechaRevision(DateUtils.obtenerFechaSistema());
        repository.save(corte);
        return convertToDTO(corte);
    }

    private void validarRevision(FinalizarRevisionCorteRequest.DetalleRevision revision) {
        String estado = revision.getRevisionEstado();
        if (estado == null || (!"OK".equalsIgnoreCase(estado) && !"SUGERENCIA".equalsIgnoreCase(estado))) {
            throw new IllegalArgumentException("Cada detalle debe marcarse OK o SUGERENCIA.");
        }
        if ("SUGERENCIA".equalsIgnoreCase(estado)
                && (revision.getRevisionComentario() == null
                || revision.getRevisionComentario().trim().isEmpty())) {
            throw new IllegalArgumentException("La sugerencia requiere comentario.");
        }
    }

    @Override
    public CorteVentaRangoResponse consultarRango(CorteVentaRangoRequest request) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: consultarRango - fechaIni: {} - fechaFin: {} - ultimoCorte: {} - actual: {}", request.getFechaIni(), request.getFechaFin(), request.isUltimoCorte(), request.isActual());
        
        // Validaciones manuales según requerimiento
        if (!request.isUltimoCorte() && request.getFechaIni() == null) {
            throw new IllegalArgumentException("Si el campo 'ultimoCorte' es falso, 'fechaIni' no puede ser nulo.");
        }
        if (!request.isActual() && request.getFechaFin() == null) {
            throw new IllegalArgumentException("Si el campo 'actual' es falso, 'fechaFin' no puede ser nulo.");
        }

        CorteVentaRangoResponse response = new CorteVentaRangoResponse();
        List<CorteVentaDTO> otrosCortes = new ArrayList<>();
        Long movimientoAfterId = 0L;
        Long historialAfterId = 0L;
        Map<Long, BigDecimal> basePorMedio = new HashMap<>();
        CorteVenta ultimoCorteVentaRef = null;

        if (request.isUltimoCorte()) {
            Optional<CorteVenta> ultimoCorteVentaOpt =
                    repository.findFirstByEstadoNotOrderByFechaCreacionDesc("eliminado");
            Optional<HistorialRecibo> posteriorHistorialRecibo;

            if (ultimoCorteVentaOpt.isPresent()) {
                CorteVenta ultimoCorteVenta = ultimoCorteVentaOpt.get();
                ultimoCorteVentaRef = ultimoCorteVenta;
                response.setUltimoCorte(ultimoCorteVenta.getFechaCreacion());
                if (ultimoCorteVenta.getUltimoMovimientoOrigenFondosId() != null) {
                    movimientoAfterId = ultimoCorteVenta.getUltimoMovimientoOrigenFondosId();
                }
                if (ultimoCorteVenta.getUltimoHistorialReciboId() != null) {
                    historialAfterId = ultimoCorteVenta.getUltimoHistorialReciboId();
                }
                // Base: preferir base_siguiente_efectivo del corte anterior (post-distribución);
                // si no, físico declarado del detalle.
                if (ultimoCorteVenta.getBaseSiguienteEfectivo() != null) {
                    OrigenFondos cajaEfectivo = resolverOrigenPorNombres("Caja: Efectivo", "Caja Efectivo");
                    if (cajaEfectivo != null && cajaEfectivo.getMetodoPagoId() != null) {
                        basePorMedio.put(cajaEfectivo.getMetodoPagoId(), ultimoCorteVenta.getBaseSiguienteEfectivo());
                    }
                }
                for (CorteVentaDetalle det : detalleRepository
                        .findByCorteVentaIdOrderByOrdenAscIdAsc(ultimoCorteVenta.getId())) {
                    if (det.getMetodoPagoId() != null && det.getTotal() != null
                            && !basePorMedio.containsKey(det.getMetodoPagoId())) {
                        basePorMedio.put(det.getMetodoPagoId(), det.getTotal());
                    }
                }

                // Watermark por id: evita recontar el último ticket del corte
                // (fechaIni inclusiva coincidía con fecha_fin del corte).
                if (historialAfterId > 0L) {
                    posteriorHistorialRecibo = historialReciboRepository
                            .findFirstByIdGreaterThanOrderByIdAsc(historialAfterId);
                } else {
                    List<HistorialRecibo> posibles =
                            historialReciboRepository.findAllPosteriorAFecha(ultimoCorteVenta.getFechaFin());
                    LocalDateTime fechaFinMinutos = ultimoCorteVenta.getFechaFin().withSecond(0).withNano(0);
                    posteriorHistorialRecibo = posibles.stream()
                            .filter(hr -> hr.getFechaCreacion().withSecond(0).withNano(0).isAfter(fechaFinMinutos))
                            .findFirst();
                    if (posteriorHistorialRecibo.isEmpty() && !posibles.isEmpty()) {
                        posteriorHistorialRecibo = posibles.stream()
                                .filter(hr -> hr.getId() > 0)
                                .filter(hr -> hr.getFechaCreacion().isAfter(ultimoCorteVenta.getFechaFin()))
                                .findFirst();
                    }
                }
            } else {
                // Sin corte previo: Base del primer turno = inversión inicial (si existe).
                Optional<MovimientoOrigenFondos> baseInicialOpt =
                        movimientoOrigenFondosRepository.findFirstByOrigenTipoOrderByIdAsc(
                                MovimientoOrigenFondosServiceImpl.ORIGEN_TIPO_BASE_INICIAL);
                if (baseInicialOpt.isPresent()) {
                    MovimientoOrigenFondos bi = baseInicialOpt.get();
                    movimientoAfterId = bi.getId();
                    OrigenFondos cajaEfectivo = resolverOrigenPorNombres("Caja: Efectivo", "Caja Efectivo");
                    if (cajaEfectivo != null && cajaEfectivo.getMetodoPagoId() != null && bi.getValor() != null) {
                        basePorMedio.put(cajaEfectivo.getMetodoPagoId(), bi.getValor());
                    }
                }
                posteriorHistorialRecibo = historialReciboRepository.findFirstByOrderByFechaCreacionAsc();
            }

            // Desde = primera actividad del turno abierto (venta o movimiento de O.F.).
            LocalDateTime primeraVenta = posteriorHistorialRecibo
                    .map(HistorialRecibo::getFechaCreacion)
                    .orElse(null);
            LocalDateTime primerMovimiento = movimientoOrigenFondosRepository
                    .findMinFechaCreacionByIdGreaterThan(movimientoAfterId)
                    .orElse(null);
            LocalDateTime fechaIniTurno = minDateTime(primeraVenta, primerMovimiento);
            if (fechaIniTurno != null) {
                response.setFechaIni(fechaIniTurno);
            } else if (ultimoCorteVentaRef != null) {
                // Sin actividad aún: el periodo arranca al cierre del corte anterior.
                response.setFechaIni(ultimoCorteVentaRef.getFechaFin());
            } else if (movimientoAfterId != null && movimientoAfterId > 0L) {
                // Instalación: periodo arranca en la fecha de la inversión inicial.
                movimientoOrigenFondosRepository.findById(movimientoAfterId)
                        .ifPresent(m -> response.setFechaIni(m.getFechaCreacion()));
            }

            if (request.isActual()) {
                // Hasta = última actividad del turno (venta o movimiento), no solo el último ticket.
                LocalDateTime ultimaVenta = historialReciboRepository
                        .findMaxFechaCreacionByIdGreaterThan(historialAfterId)
                        .orElse(null);
                LocalDateTime ultimoMovimiento = movimientoOrigenFondosRepository
                        .findMaxFechaCreacionByIdGreaterThan(movimientoAfterId)
                        .orElse(null);
                LocalDateTime fechaFinTurno = maxDateTime(ultimaVenta, ultimoMovimiento);
                if (fechaFinTurno != null) {
                    response.setFechaFin(fechaFinTurno);
                } else if (response.getFechaIni() != null) {
                    // Sin actividad nueva: no reutilizar la fecha del último ticket del corte anterior.
                    response.setFechaFin(DateUtils.obtenerFechaSistema());
                }
            } else {
                response.setFechaFin(request.getFechaFin());
            }
        } else {
            response.setFechaIni(request.getFechaIni());
            response.setFechaFin(request.getFechaFin());
            
            // Consultar cortes intersectados
            List<CorteVenta> intersectados = repository.findByFechaIniBetweenOrFechaFinBetween(
                    request.getFechaIni(), request.getFechaFin(),
                    request.getFechaIni(), request.getFechaFin());
            
            otrosCortes = intersectados.stream()
                    .filter(c -> !"eliminado".equals(c.getEstado()))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        response.setOtrosCortesIntersectados(otrosCortes);

        // Consultar resumen de ventas, egresos y movimientos por método de pago
        if (response.getFechaIni() != null && response.getFechaFin() != null) {
            LocalDateTime finVentas = response.getFechaFin();
            LocalDateTime finEgresos = finVentas;
            if (request.isActual()) {
                LocalDateTime ahora = DateUtils.obtenerFechaSistema();
                if (finEgresos.isBefore(ahora)) {
                    finEgresos = ahora;
                }
            }

            Map<Long, BigDecimal> ventasPorMedio;
            if (request.isUltimoCorte()) {
                // Watermark por id: no recontar tickets ya incluidos en el corte anterior.
                LocalDateTime finVentasWatermark = finEgresos;
                ventasPorMedio = toResumenMap(
                        historialReciboPagoRepository.findResumenVentasPorMetodoPagoAfterId(
                                historialAfterId, finVentasWatermark));
            } else {
                ventasPorMedio = toResumenMap(
                        historialReciboPagoRepository.findResumenVentasPorMetodoPago(
                                response.getFechaIni(), finVentas));
            }
            Map<Long, BigDecimal> egresosPorMedio = toResumenMap(
                    egresoRepository.findResumenEgresosPorMetodoPago(
                            response.getFechaIni(),
                            finEgresos,
                            response.getFechaIni().toLocalDate(),
                            finEgresos.toLocalDate()));

            List<TipoMovimientoOrigenFondos> excluidosMov = Arrays.asList(
                    TipoMovimientoOrigenFondos.SALIDA_EGRESO,
                    TipoMovimientoOrigenFondos.ENTRADA_VENTA,
                    TipoMovimientoOrigenFondos.REVERSO_ENTRADA_VENTA
            );
            Map<Long, BigDecimal> movimientosPorMedio;
            if (request.isUltimoCorte()) {
                // Watermark por id: incluye movimientos entre el corte y la 1ª venta.
                movimientosPorMedio = toResumenMap(
                        movimientoOrigenFondosRepository.findResumenMovimientosPorMetodoPagoAfterId(
                                movimientoAfterId,
                                finEgresos,
                                excluidosMov));
            } else {
                movimientosPorMedio = toResumenMap(
                        movimientoOrigenFondosRepository.findResumenMovimientosPorMetodoPago(
                                response.getFechaIni(),
                                finEgresos,
                                excluidosMov));
            }

            Set<Long> medios = new HashSet<>();
            medios.addAll(ventasPorMedio.keySet());
            medios.addAll(egresosPorMedio.keySet());
            medios.addAll(movimientosPorMedio.keySet());
            medios.addAll(basePorMedio.keySet());

            List<CorteVentaRangoResponse.VentasTipoResumenDTO> ventasTipo = medios.stream()
                    .sorted()
                    .map(metodoPagoId -> {
                        BigDecimal base = basePorMedio.getOrDefault(metodoPagoId, BigDecimal.ZERO);
                        BigDecimal ventas = ventasPorMedio.getOrDefault(metodoPagoId, BigDecimal.ZERO);
                        BigDecimal egresos = egresosPorMedio.getOrDefault(metodoPagoId, BigDecimal.ZERO);
                        BigDecimal movimientos = movimientosPorMedio.getOrDefault(metodoPagoId, BigDecimal.ZERO);
                        return CorteVentaRangoResponse.VentasTipoResumenDTO.builder()
                                .metodoPagoId(metodoPagoId)
                                .base(base)
                                .totalVentasSistema(ventas)
                                .totalEgresosSistema(egresos)
                                .totalMovimientosSistema(movimientos)
                                .totalSistema(base.add(ventas).subtract(egresos).add(movimientos))
                                .build();
                    })
                    .collect(Collectors.toList());

            response.setVentasTipo(ventasTipo);

            BigDecimal total = ventasTipo.stream()
                    .map(CorteVentaRangoResponse.VentasTipoResumenDTO::getTotalSistema)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotal(total);
        } else {
            response.setVentasTipo(Collections.emptyList());
            response.setTotal(BigDecimal.ZERO);
        }

        return response;
    }

    private static LocalDateTime minDateTime(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }

    private static LocalDateTime maxDateTime(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    private Map<Long, BigDecimal> toResumenMap(List<Object[]> rows) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            map.put((Long) row[0], row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO);
        }
        return map;
    }

    @Override
    public List<CorteVentaDTO> search(LocalDateTime fechaIni, LocalDateTime fechaFin) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: search - fechaIni: {} - fechaFin: {}", fechaIni, fechaFin);
        return repository.findByFechaIniGreaterThanEqualAndFechaIniLessThanEqual(fechaIni, fechaFin)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CorteVentaDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> distinct = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, CorteVentaDTO> byId = repository.findByIdIn(distinct).stream()
                .map(this::convertToDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(CorteVentaDTO::getId, d -> d, (a, b) -> a));
        // Conservar el orden pedido por el cliente.
        return distinct.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public CorteVentaDTO convertToDTO(CorteVenta entity) {
        if (entity == null) return null;
        
        List<VentasTipoDTO> ventasTipoDTOList = entity.getVentasTipo() != null ?
                entity.getVentasTipo().stream()
                        .map(vt -> VentasTipoDTO.builder()
                                .id(vt.getId())
                                .metodoPagoId(vt.getMetodoPagoId())
                                .total(vt.getTotal())
                                .totalSistema(vt.getTotalSistema())
                                .totalVentasSistema(vt.getTotalVentasSistema())
                                .totalEgresosSistema(vt.getTotalEgresosSistema())
                                .desfase(vt.getDesfase())
                                .motivoDesfaseId(vt.getMotivoDesfaseId())
                                .corteVentaId(vt.getCorteVentaId())
                                .build())
                        .collect(Collectors.toList()) : Collections.emptyList();

        List<CorteVentaDetalleDTO> detalles = entity.getId() == null
                ? Collections.emptyList()
                : detalleRepository.findByCorteVentaIdOrderByOrdenAscIdAsc(entity.getId()).stream()
                        .map(this::toDetalleDTO)
                        .collect(Collectors.toList());
        boolean ultimoVigente = entity.getId() != null
                && repository.findFirstByEstadoNotOrderByIdDesc("eliminado")
                        .map(c -> c.getId().equals(entity.getId()))
                        .orElse(false);

        return CorteVentaDTO.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .fechaIni(entity.getFechaIni())
                .fechaFin(entity.getFechaFin())
                .ultimoHistorialReciboId(entity.getUltimoHistorialReciboId())
                .ultimoMovimientoOrigenFondosId(entity.getUltimoMovimientoOrigenFondosId())
                .distribucionEfectivoEstado(entity.getDistribucionEfectivoEstado())
                .baseSiguienteEfectivo(entity.getBaseSiguienteEfectivo())
                .total(entity.getTotal())
                .totalSistema(entity.getTotalSistema())
                .ventasTipo(ventasTipoDTOList)
                .detalles(detalles)
                .estado(entity.getEstado())
                .observacion(entity.getObservacion())
                .revisadoPor(entity.getRevisadoPor())
                .fechaRevision(entity.getFechaRevision())
                .ultimoVigente(ultimoVigente)
                .build();
    }

    private CorteVentaDetalleDTO toDetalleDTO(CorteVentaDetalle d) {
        return CorteVentaDetalleDTO.builder()
                .id(d.getId())
                .corteVentaId(d.getCorteVentaId())
                .metodoPagoId(d.getMetodoPagoId())
                .origenFondosId(d.getOrigenFondosId())
                .base(d.getBase())
                .totalVentasSistema(d.getTotalVentasSistema())
                .totalEgresosSistema(d.getTotalEgresosSistema())
                .totalMovimientosSistema(d.getTotalMovimientosSistema())
                .totalSistema(d.getTotalSistema())
                .total(d.getTotal())
                .desfase(d.getDesfase())
                .motivoDesfaseId(d.getMotivoDesfaseId())
                .modoCaptura(d.getModoCaptura())
                .declaradoPor(d.getDeclaradoPor())
                .revisionEstado(d.getRevisionEstado())
                .revisionComentario(d.getRevisionComentario())
                .ajusteGenerado(d.getAjusteGenerado())
                .orden(d.getOrden())
                .build();
    }

    @Override
    public CorteVenta convertToEntity(CorteVentaDTO dto) {
        if (dto == null) return null;

        List<VentasTipo> ventasTipoList = dto.getVentasTipo() != null ?
                dto.getVentasTipo().stream()
                        .map(vtDto -> VentasTipo.builder()
                                .id(vtDto.getId())
                                .metodoPagoId(vtDto.getMetodoPagoId())
                                .total(vtDto.getTotal())
                                .totalSistema(vtDto.getTotalSistema())
                                .totalVentasSistema(vtDto.getTotalVentasSistema())
                                .totalEgresosSistema(vtDto.getTotalEgresosSistema())
                                .desfase(vtDto.getDesfase())
                                .motivoDesfaseId(vtDto.getMotivoDesfaseId())
                                .corteVentaId(vtDto.getCorteVentaId())
                                .build())
                        .collect(Collectors.toList()) : null;

        return CorteVenta.builder()
                .id(dto.getId())
                .usuarioId(dto.getUsuarioId())
                .fechaIni(dto.getFechaIni())
                .fechaFin(dto.getFechaFin())
                .ultimoHistorialReciboId(dto.getUltimoHistorialReciboId())
                .ultimoMovimientoOrigenFondosId(dto.getUltimoMovimientoOrigenFondosId())
                .distribucionEfectivoEstado(dto.getDistribucionEfectivoEstado())
                .baseSiguienteEfectivo(dto.getBaseSiguienteEfectivo())
                .total(dto.getTotal())
                .totalSistema(dto.getTotalSistema())
                .estado(dto.getEstado() != null ? dto.getEstado() : "creada")
                .observacion(dto.getObservacion())
                .revisadoPor(dto.getRevisadoPor())
                .fechaRevision(dto.getFechaRevision())
                .ventasTipo(ventasTipoList)
                .build();
    }

    @Override
    @Transactional
    public DistribucionEfectivoPendienteDto obtenerDistribucionPendiente() {
        Optional<CorteVenta> ultimoOpt =
                repository.findFirstByEstadoNotOrderByFechaCreacionDesc("eliminado");
        if (ultimoOpt.isEmpty()) {
            return DistribucionEfectivoPendienteDto.builder().pendiente(false).build();
        }
        CorteVenta corte = ultimoOpt.get();
        if (!"PENDIENTE".equals(corte.getDistribucionEfectivoEstado())) {
            return DistribucionEfectivoPendienteDto.builder().pendiente(false).build();
        }
        // Asegura ventas del corte en ledger (cortes legacy sin ENTRADA_VENTA).
        asegurarEntradasVentaCorte(corte);
        OrigenFondos caja = requireOrigenPorNombres("Caja: Efectivo", "Caja Efectivo");
        OrigenFondos menor = requireOrigenPorNombres("Caja Menor");
        OrigenFondos general = requireOrigenPorNombres("Caja General", "Caja general / Fondo administracion");
        BigDecimal saldo = movimientoOrigenFondosService.calcularSaldo(caja.getId());
        return DistribucionEfectivoPendienteDto.builder()
                .pendiente(true)
                .corteVentaId(corte.getId())
                .cajaEfectivoId(caja.getId())
                .cajaEfectivoNombre(caja.getNombre())
                .saldoCajaEfectivo(saldo)
                .cajaMenorId(menor.getId())
                .cajaMenorNombre(menor.getNombre())
                .cajaGeneralId(general.getId())
                .cajaGeneralNombre(general.getNombre())
                .build();
    }

    @Override
    @Transactional
    public DistribucionEfectivoResultDto confirmarDistribucionEfectivo(
            Long corteId, DistribucionEfectivoRequest request) {
        if (!isAdmin()) {
            throw new IllegalArgumentException("Solo un administrador puede confirmar la distribución de efectivo.");
        }
        CorteVenta corte = repository.findById(corteId)
                .orElseThrow(() -> new IllegalArgumentException("Corte no encontrado: " + corteId));
        if (!"PENDIENTE".equals(corte.getDistribucionEfectivoEstado())) {
            throw new IllegalArgumentException("El corte no tiene distribución pendiente.");
        }
        asegurarEntradasVentaCorte(corte);

        BigDecimal base = nz(request.getBase());
        BigDecimal aMenor = nz(request.getMontoCajaMenor());
        BigDecimal aGeneral = nz(request.getMontoCajaGeneral());
        if (base.compareTo(BigDecimal.ZERO) < 0 || aMenor.compareTo(BigDecimal.ZERO) < 0
                || aGeneral.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los montos no pueden ser negativos.");
        }

        OrigenFondos caja = requireOrigenPorNombres("Caja: Efectivo", "Caja Efectivo");
        OrigenFondos menor = requireOrigenPorNombres("Caja Menor");
        OrigenFondos general = requireOrigenPorNombres("Caja General", "Caja general / Fondo administracion");
        BigDecimal saldo = movimientoOrigenFondosService.calcularSaldo(caja.getId());
        BigDecimal suma = base.add(aMenor).add(aGeneral);
        if (suma.compareTo(saldo) != 0) {
            throw new IllegalArgumentException(
                    "Base + Caja Menor + Caja General debe igualar el saldo de Caja: Efectivo ("
                            + saldo + "). Suma enviada: " + suma);
        }

        List<MovimientoOrigenFondosDto> movimientos = new ArrayList<>();
        String obs = request.getObservacion();
        if (aMenor.compareTo(BigDecimal.ZERO) > 0) {
            movimientos.addAll(movimientoOrigenFondosService.registrarTrasladoDistribucion(
                    caja.getId(), menor.getId(), aMenor, corteId, obs));
        }
        if (aGeneral.compareTo(BigDecimal.ZERO) > 0) {
            movimientos.addAll(movimientoOrigenFondosService.registrarTrasladoDistribucion(
                    caja.getId(), general.getId(), aGeneral, corteId, obs));
        }

        BigDecimal saldoFinal = movimientoOrigenFondosService.calcularSaldo(caja.getId());
        if (saldoFinal.compareTo(base) != 0) {
            throw new IllegalStateException(
                    "Saldo final de Caja (" + saldoFinal + ") no coincide con la base declarada (" + base + ").");
        }

        corte.setBaseSiguienteEfectivo(base);
        corte.setDistribucionEfectivoEstado("CONFIRMADA");
        movimientoOrigenFondosRepository.findMaxId()
                .ifPresent(corte::setUltimoMovimientoOrigenFondosId);
        repository.save(corte);

        return DistribucionEfectivoResultDto.builder()
                .ok(true)
                .corteVentaId(corteId)
                .baseSiguienteEfectivo(base)
                .movimientos(movimientos)
                .build();
    }

    /**
     * Contabiliza ventas del corte en el ledger si aún no existen (idempotente).
     */
    private void asegurarEntradasVentaCorte(CorteVenta corte) {
        List<CorteVentaDetalle> detalles =
                detalleRepository.findByCorteVentaIdOrderByOrdenAscIdAsc(corte.getId());
        movimientoOrigenFondosService.registrarEntradasVentaCorte(corte.getId(), detalles);
        if ("PENDIENTE".equals(corte.getDistribucionEfectivoEstado())) {
            movimientoOrigenFondosRepository.findMaxId().ifPresent(maxId -> {
                Long actual = corte.getUltimoMovimientoOrigenFondosId();
                if (actual == null || maxId > actual) {
                    corte.setUltimoMovimientoOrigenFondosId(maxId);
                    repository.save(corte);
                }
            });
        }
    }

    @Override
    public BaseInicialPendienteDto obtenerBaseInicialPendiente() {
        boolean hayCorte = repository.findFirstByEstadoNotOrderByFechaCreacionDesc("eliminado").isPresent();
        boolean hayBaseInicial = movimientoOrigenFondosRepository.existsByOrigenTipo(
                MovimientoOrigenFondosServiceImpl.ORIGEN_TIPO_BASE_INICIAL);
        if (hayCorte || hayBaseInicial) {
            return BaseInicialPendienteDto.builder().pendiente(false).build();
        }
        OrigenFondos caja = requireOrigenPorNombres("Caja: Efectivo", "Caja Efectivo");
        MotivoMovimiento motivo = motivoMovimientoRepository
                .findByCodigo(MovimientoOrigenFondosServiceImpl.CODIGO_MOTIVO_INVERSION_INICIAL)
                .orElse(null);
        if (motivo == null) {
            return BaseInicialPendienteDto.builder().pendiente(false).build();
        }
        return BaseInicialPendienteDto.builder()
                .pendiente(true)
                .cajaEfectivoId(caja.getId())
                .cajaEfectivoNombre(caja.getNombre())
                .motivoMovimientoId(motivo.getId())
                .motivoMovimientoNombre(motivo.getNombre())
                .build();
    }

    @Override
    @Transactional
    public BaseInicialResultDto confirmarBaseInicial(BaseInicialRequest request) {
        if (!isAdmin()) {
            throw new IllegalArgumentException("Solo un administrador puede registrar la base inicial.");
        }
        BaseInicialPendienteDto pendiente = obtenerBaseInicialPendiente();
        if (!pendiente.isPendiente()) {
            throw new IllegalArgumentException("No hay base inicial pendiente de registrar.");
        }
        MovimientoOrigenFondosDto mov = movimientoOrigenFondosService.registrarBaseInicial(request);
        return BaseInicialResultDto.builder()
                .ok(true)
                .baseSiguienteEfectivo(mov.getValor())
                .movimientoId(mov.getId())
                .build();
    }

    private OrigenFondos resolverOrigenPorNombres(String... nombres) {
        List<OrigenFondos> todos = origenFondosRepository.findByActivoTrueOrderByOrdenAscIdAsc();
        for (String nombre : nombres) {
            for (OrigenFondos o : todos) {
                if (o.getNombre() != null && o.getNombre().equalsIgnoreCase(nombre.trim())) {
                    return o;
                }
            }
        }
        return null;
    }

    private OrigenFondos requireOrigenPorNombres(String... nombres) {
        OrigenFondos found = resolverOrigenPorNombres(nombres);
        if (found == null) {
            throw new IllegalArgumentException(
                    "No se encontró origen de fondos: " + String.join(" / ", nombres));
        }
        return found;
    }

    private boolean isAdmin() {
        return SecurityContextHelper.getUser() != null
                && SecurityContextHelper.getUser().getRoles() != null
                && SecurityContextHelper.getUser().getRoles().stream()
                        .anyMatch(r -> "admin".equalsIgnoreCase(r.getSigla())
                                || "admin".equalsIgnoreCase(r.getNombre()));
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private boolean sameMoney(BigDecimal a, BigDecimal b) {
        return a == null ? b == null : b != null && a.compareTo(b) == 0;
    }
}