package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.EstadisticaAnualResponse;
import com.infinitesoft.pos_relational_data_service.dto.EstadisticaDiariaResponse;
import com.infinitesoft.pos_relational_data_service.dto.EstadisticaMensualResponse;
import com.infinitesoft.pos_relational_data_service.entities.EstadisticaFin;
import com.infinitesoft.pos_relational_data_service.entities.TipoResultadoFin;
import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoMovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.repositories.EstadisticaFinRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TipoResultadoFinRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.CorteVentaDetalleRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MovimientoOrigenFondosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstadisticaFinancieraService {

    private final EstadisticaFinRepository estadisticaFinRepository;
    private final TipoResultadoFinRepository tipoResultadoFinRepository;
    private final EgresoRepository egresoRepository;
    private final CorteVentaDetalleRepository corteVentaDetalleRepository;
    private final MovimientoOrigenFondosRepository movimientoOrigenFondosRepository;

    public String extraerFormatoDesdeValorTiempo(String valorTiempo) {
        if (valorTiempo == null) return null;
        if (valorTiempo.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return "DIA";
        } else if (valorTiempo.matches("^\\d{4}-\\d{2}$")) {
            return "MES";
        } else if (valorTiempo.matches("^\\d{4}$")) {
            return "ANIO";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de valorTiempo no reconocido: " + valorTiempo);
    }

    public boolean existeEstadistica(String valorTiempo) {
        String formato = extraerFormatoDesdeValorTiempo(valorTiempo);
        return estadisticaFinRepository.findByValorTiempoAndFormatoTiempo(valorTiempo, formato).isPresent();
    }

    public boolean tieneDatos(String valorTiempo) {
        String formato = extraerFormatoDesdeValorTiempo(valorTiempo);
        LocalDate fechaInicio;
        LocalDate fechaFin;

        try {
            switch (formato) {
                case "DIA":
                    fechaInicio = LocalDate.parse(valorTiempo);
                    fechaFin = fechaInicio;
                    break;
                case "MES":
                    YearMonth ym = YearMonth.parse(valorTiempo);
                    fechaInicio = ym.atDay(1);
                    fechaFin = ym.atEndOfMonth();
                    break;
                case "ANIO":
                    int year = Integer.parseInt(valorTiempo);
                    fechaInicio = LocalDate.of(year, 1, 1);
                    fechaFin = LocalDate.of(year, 12, 31);
                    break;
                default:
                    return false;
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            return false;
        }

        boolean hayEgresos = !egresoRepository.findByFechaBetween(fechaInicio, fechaFin).isEmpty();
        if (hayEgresos) {
            return true;
        }
        boolean hayCobranzas = !movimientoOrigenFondosRepository
                .findByTipoMovimientoAndFechaBetween(
                        TipoMovimientoOrigenFondos.ENTRADA_COBRANZA, fechaInicio, fechaFin)
                .isEmpty();
        if (hayCobranzas) {
            return true;
        }
        LocalDateTime ldtInicio = fechaInicio.atStartOfDay();
        LocalDateTime ldtFin = fechaFin.atTime(23, 59, 59);
        BigDecimal ventasCorte = corteVentaDetalleRepository
                .sumVentasSistemaCortesVigentes(ldtInicio, ldtFin);
        return ventasCorte != null && ventasCorte.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Recalcula día, mes y año de una fecha de negocio (egreso, corte, PUT).
     */
    @Transactional
    public void sincronizarPeriodosDeFecha(LocalDate fecha) {
        if (fecha == null) {
            return;
        }
        crearOActualizarEstadisticaSync(fecha.format(DateTimeFormatter.ISO_LOCAL_DATE));
        crearOActualizarEstadisticaSync(YearMonth.from(fecha).toString());
        crearOActualizarEstadisticaSync(String.valueOf(fecha.getYear()));
    }

    @Async
    @Transactional
    public void crearEstadisticaAsync(String valorTiempo) {
        try {
            log.info("Iniciando creación asíncrona de estadística para: {}", valorTiempo);
            String formato = extraerFormatoDesdeValorTiempo(valorTiempo);
            if (estadisticaFinRepository.findByValorTiempoAndFormatoTiempo(valorTiempo, formato).isPresent()) {
                log.info("Estadistica ya existe para {} y {}. Proceso omitido.", valorTiempo, formato);
                return;
            }
            EstadisticaFin nueva = calcularYGuardarEstadistica(valorTiempo, formato, null);
            if (nueva != null) {
                log.info("Finalizada creación asíncrona de estadística para: {}", valorTiempo);
            } else {
                log.info("No se creó estadística para {} porque totalVentas y totalEgresos son nulos", valorTiempo);
            }
        } catch (Exception e) {
            log.error("Error en proceso asíncrono para {}: {}", valorTiempo, e.getMessage(), e);
        }
    }

    @Transactional
    public EstadisticaFin actualizarEstadisticaSync(String valorTiempo) {
        String formato = extraerFormatoDesdeValorTiempo(valorTiempo);
        EstadisticaFin existente = estadisticaFinRepository.findByValorTiempoAndFormatoTiempo(valorTiempo, formato)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró estadística para actualizar"));
        return calcularYGuardarEstadistica(valorTiempo, formato, existente);
    }

    @Transactional
    public EstadisticaFin crearOActualizarEstadisticaSync(String valorTiempo) {
        String formato = extraerFormatoDesdeValorTiempo(valorTiempo);
        EstadisticaFin existente = estadisticaFinRepository.findByValorTiempoAndFormatoTiempo(valorTiempo, formato)
                .orElse(null);
        return calcularYGuardarEstadistica(valorTiempo, formato, existente);
    }

    private EstadisticaFin calcularYGuardarEstadistica(String valorTiempo, String formato, EstadisticaFin existente) {
        LocalDate fechaInicio;
        LocalDate fechaFin;

        try {
            switch (formato) {
                case "DIA":
                    fechaInicio = LocalDate.parse(valorTiempo);
                    fechaFin = fechaInicio;
                    break;
                case "MES":
                    YearMonth ym = YearMonth.parse(valorTiempo);
                    fechaInicio = ym.atDay(1);
                    fechaFin = ym.atEndOfMonth();
                    break;
                case "ANIO":
                    int year = Integer.parseInt(valorTiempo);
                    fechaInicio = LocalDate.of(year, 1, 1);
                    fechaFin = LocalDate.of(year, 12, 31);
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato no soportado");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al parsear valorTiempo: " + valorTiempo);
        }

        List<Egreso> egresos = egresoRepository.findByFechaBetween(fechaInicio, fechaFin);
        BigDecimal totalEgresos = egresos.stream()
                .map(Egreso::getValor)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime ldtInicio = fechaInicio.atStartOfDay();
        LocalDateTime ldtFin = fechaFin.atTime(23, 59, 59);
        BigDecimal totalVentas = nz(corteVentaDetalleRepository
                .sumVentasSistemaCortesVigentes(ldtInicio, ldtFin));

        BigDecimal totalCobranzas = movimientoOrigenFondosRepository
                .findByTipoMovimientoAndFechaBetween(
                        TipoMovimientoOrigenFondos.ENTRADA_COBRANZA, fechaInicio, fechaFin)
                .stream()
                .map(m -> m.getImpacto() != null ? m.getImpacto() : m.getValor())
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean hayIngresos = totalVentas.compareTo(BigDecimal.ZERO) > 0
                || totalCobranzas.compareTo(BigDecimal.ZERO) > 0;
        boolean hayEgresos = totalEgresos.compareTo(BigDecimal.ZERO) > 0;
        if (!hayIngresos && !hayEgresos) {
            return null;
        }

        BigDecimal ingresos = totalVentas.add(totalCobranzas);
        BigDecimal utilidad = ingresos.subtract(totalEgresos);
        BigDecimal porcentajeUtilidad = null;
        if (ingresos.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeUtilidad = utilidad.multiply(new BigDecimal("100"))
                    .divide(ingresos, 2, RoundingMode.HALF_UP);
        } else if (hayEgresos) {
            porcentajeUtilidad = new BigDecimal("-100");
        }

        String sigla = determinarSigla(ingresos, totalEgresos, porcentajeUtilidad);
        TipoResultadoFin tipo = tipoResultadoFinRepository.findFirstBySigla(sigla)
                .orElse(null);

        EstadisticaFin estadistica = existente != null ? existente : new EstadisticaFin();
        estadistica.setValorTiempo(valorTiempo);
        estadistica.setFormatoTiempo(formato);
        estadistica.setTotalEgresos(totalEgresos);
        estadistica.setTotalVentas(totalVentas);
        estadistica.setTotalCobranzas(totalCobranzas);
        estadistica.setUtilidad(utilidad);
        estadistica.setPorcentajeUtilidad(porcentajeUtilidad);
        estadistica.setTipoResultadoFin(tipo);

        return estadisticaFinRepository.save(estadistica);
    }


    public Page<EstadisticaDiariaResponse> listarDiarias(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        log.info("Consultando estadísticas diarias paginadas con rango: {} - {}", fechaInicio, fechaFin);
        String start = fechaInicio != null ? fechaInicio.toString() : null;
        String end = fechaFin != null ? fechaFin.toString() : null;
        return estadisticaFinRepository.findByFormatoTiempoWithRange("DIA", start, end, pageable)
                .map(this::convertToDiariaResponse);
    }

    public List<EstadisticaMensualResponse> listarMensuales(String mesInicio, String mesFin) {
        log.info("Consultando estadísticas mensuales con rango: {} - {}", mesInicio, mesFin);
        return estadisticaFinRepository.findByFormatoTiempoWithRangeList("MES", mesInicio, mesFin).stream()
                .map(this::convertToMensualResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<EstadisticaAnualResponse> listarAnuales(String anioInicio, String anioFin) {
        log.info("Consultando estadísticas anuales con rango: {} - {}", anioInicio, anioFin);
        return estadisticaFinRepository.findByFormatoTiempoWithRangeList("ANIO", anioInicio, anioFin).stream()
                .map(this::convertToAnualResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    private EstadisticaDiariaResponse convertToDiariaResponse(EstadisticaFin e) {
        return EstadisticaDiariaResponse.builder()
                .id(e.getId())
                .fechaCreacion(e.getFechaCreacion())
                .totalEgresos(e.getTotalEgresos())
                .totalVentas(e.getTotalVentas())
                .totalCobranzas(e.getTotalCobranzas())
                .utilidad(e.getUtilidad())
                .porcentajeUtilidad(e.getPorcentajeUtilidad())
                .valorTiempo(e.getValorTiempo())
                .dia(java.sql.Date.valueOf(LocalDate.parse(e.getValorTiempo())))
                .tipoResultadoFin(e.getTipoResultadoFin())
                .build();
    }

    private EstadisticaMensualResponse convertToMensualResponse(EstadisticaFin e) {
        return EstadisticaMensualResponse.builder()
                .id(e.getId())
                .fechaCreacion(e.getFechaCreacion())
                .totalEgresos(e.getTotalEgresos())
                .totalVentas(e.getTotalVentas())
                .totalCobranzas(e.getTotalCobranzas())
                .utilidad(e.getUtilidad())
                .porcentajeUtilidad(e.getPorcentajeUtilidad())
                .valorTiempo(e.getValorTiempo())
                .mes(java.sql.Date.valueOf(YearMonth.parse(e.getValorTiempo()).atDay(1)))
                .tipoResultadoFin(e.getTipoResultadoFin())
                .build();
    }

    private EstadisticaAnualResponse convertToAnualResponse(EstadisticaFin e) {
        return EstadisticaAnualResponse.builder()
                .id(e.getId())
                .fechaCreacion(e.getFechaCreacion())
                .totalEgresos(e.getTotalEgresos())
                .totalVentas(e.getTotalVentas())
                .totalCobranzas(e.getTotalCobranzas())
                .utilidad(e.getUtilidad())
                .porcentajeUtilidad(e.getPorcentajeUtilidad())
                .valorTiempo(e.getValorTiempo())
                .anio(java.sql.Date.valueOf(LocalDate.of(Integer.parseInt(e.getValorTiempo()), 1, 1)))
                .tipoResultadoFin(e.getTipoResultadoFin())
                .build();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String determinarSigla(BigDecimal totalVentas, BigDecimal totalEgresos, BigDecimal porcentaje) {
        if (totalVentas == null || totalVentas.compareTo(BigDecimal.ZERO) <= 0) return "no_ventas";
        if (totalEgresos == null || totalEgresos.compareTo(BigDecimal.ZERO) <= 0) return "no_egresos";
        if (porcentaje == null) return "no_ventas";

        double p = porcentaje.doubleValue();

        if (p >= 0) {
            if (p < 5) return "util_menos_5p";
            if (p < 10) return "util_5p_10p";
            if (p < 20) return "util_10p_20p";
            if (p < 30) return "util_20p_30p";
            if (p < 40) return "util_30p_40p";
            if (p < 50) return "util_40p_50p";
            if (p < 60) return "util_50p_60p";
            if (p < 70) return "util_60p_70p";
            return "util_mayor_70p";
        } else {
            double perdida = Math.abs(p);
            if (perdida < 5) return "p_menos_5p";
            if (perdida < 10) return "p_5p_10p";
            if (perdida < 20) return "p_10p_20p";
            if (perdida < 30) return "p_20p_30p";
            return "p_mayor_30p";
        }
    }
}
