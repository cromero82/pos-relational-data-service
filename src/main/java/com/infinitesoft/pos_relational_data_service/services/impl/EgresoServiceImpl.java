package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;
import com.infinitesoft.pos_relational_data_service.entities.MovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.Proveedor;
import com.infinitesoft.pos_relational_data_service.entities.TipoEgreso;
import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaEgreso;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoMovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.repositories.OrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MovimientoOrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProveedorRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TipoEgresoRepository;
import com.infinitesoft.pos_relational_data_service.services.OrigenFondosService;
import com.infinitesoft.pos_relational_data_service.services.EgresoService;
import com.infinitesoft.pos_relational_data_service.services.EstadisticaFinancieraService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
public class EgresoServiceImpl implements EgresoService {

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private OrigenFondosRepository origenFondosRepository;

    @Autowired
    private OrigenFondosService origenFondosService;

    @Autowired
    private MovimientoOrigenFondosService movimientoOrigenFondosService;

    @Autowired
    private MovimientoOrigenFondosRepository movimientoOrigenFondosRepository;

    @Autowired
    private EstadisticaFinancieraService estadisticaFinancieraService;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private TipoEgresoRepository tipoEgresoRepository;

    /**
     * El método de pago es opcional (derivado del O.F. si existe).
     * Prima {@code origenFondosId}; cuentas como Caja Menor no tienen medio de pago.
     */
    private void validarMetodoPago(Egreso egreso) {
        if (egreso.getMetodoPagoId() == null) {
            return;
        }
        MetodoPago metodoPago = metodoPagoRepository.findById(egreso.getMetodoPagoId())
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado."));
        if (Boolean.FALSE.equals(metodoPago.getVisiblePagosEgresos())) {
            throw new IllegalArgumentException("El método de pago no está habilitado para egresos.");
        }
    }

    private void validarOrigenFondos(Egreso egreso) {
        if (egreso.getOrigenFondosId() == null) {
            throw new IllegalArgumentException("Debe indicar el origen del egreso (medio o origen).");
        }
        OrigenFondos cuenta = origenFondosRepository.findById(egreso.getOrigenFondosId())
                .orElseThrow(() -> new IllegalArgumentException("Origen de fondos no encontrada."));
        if (Boolean.FALSE.equals(cuenta.getActivo())) {
            throw new IllegalArgumentException("La origen de fondos no está activa.");
        }
        if (Boolean.FALSE.equals(cuenta.getVisibleEnEgreso())) {
            throw new IllegalArgumentException("La cuenta seleccionada no está habilitada para egresos.");
        }
        // Nullable: no todas las cuentas tienen método de pago vinculado.
        egreso.setMetodoPagoId(origenFondosService.resolverMetodoPagoId(egreso.getOrigenFondosId()));
    }

    @Override
    @Transactional
    public Egreso create(Egreso egreso) {
        log.info("Iniciando servicio EgresoServiceImpl - Método: create - Egreso: {}", egreso);
        aplicarFormalizarDesdeMovimiento(egreso);
        resolverTipoYNaturaleza(egreso);
        validarOrigenFondos(egreso);
        validarMetodoPago(egreso);

        LocalDate requestDate = egreso.getFecha();

        Egreso saved = egresoRepository.save(egreso);
        movimientoOrigenFondosService.registrarSalidaEgreso(saved);

        ajustarEstadisticas(requestDate);

        return saved;
    }

    /**
     * Formalizar egreso: el dinero ya está en Para ordenar (u otra bolsa por identificar).
     * Fuerza {@code origenFondosId} = OF del movimiento (impacto +) y evita doble resta del banco.
     */
    private void aplicarFormalizarDesdeMovimiento(Egreso egreso) {
        Long movId = egreso.getFromMovimientoOrigenFondosId();
        if (movId == null) {
            return;
        }
        egresoRepository.findByFromMovimientoOrigenFondosId(movId).ifPresent(existing -> {
            throw new IllegalArgumentException(
                    "Ese movimiento ya fue formalizado como egreso #" + existing.getId());
        });
        MovimientoOrigenFondos mov = movimientoOrigenFondosRepository.findById(movId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Movimiento de origen de fondos no encontrado: " + movId));
        if (mov.getImpacto() == null || mov.getImpacto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Solo se puede formalizar un movimiento con impacto positivo (entrada a la bolsa).");
        }
        String origenTipo = mov.getOrigenTipo() != null ? mov.getOrigenTipo().trim().toUpperCase() : "";
        if (!"MOVIMIENTO BANCO POR IDENTIFICAR".equals(origenTipo)) {
            throw new IllegalArgumentException(
                    "Solo movimientos «por identificar» se pueden formalizar como egreso (origenTipo="
                            + mov.getOrigenTipo() + ").");
        }
        if (mov.getTipoMovimiento() != TipoMovimientoOrigenFondos.TRASLADO
                && mov.getTipoMovimiento() != TipoMovimientoOrigenFondos.ENTRADA_MANUAL) {
            throw new IllegalArgumentException(
                    "Tipo de movimiento no formalizable: " + mov.getTipoMovimiento());
        }
        egreso.setOrigenFondosId(mov.getOrigenFondosId());
        if (egreso.getValor() == null || egreso.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            egreso.setValor(mov.getValor());
        }
        StringBuilder desc = new StringBuilder();
        if (egreso.getDescripcion() != null && !egreso.getDescripcion().isBlank()) {
            desc.append(egreso.getDescripcion().trim());
        }
        if (mov.getIdReferencia() != null) {
            if (desc.length() > 0) {
                desc.append(" · ");
            }
            desc.append("Notif #").append(mov.getIdReferencia());
        }
        desc.append(" · Formalizado mov #").append(mov.getId());
        egreso.setDescripcion(desc.toString());
        log.info(
                "Formalizar egreso desde mov={} of={} valor={} notifRef={}",
                movId, mov.getOrigenFondosId(), egreso.getValor(), mov.getIdReferencia());
    }

    @Override
    @Transactional
    public Egreso update(Long id, Egreso egreso) {
        Egreso anterior = findById(id);
        if (anterior == null) {
            return null;
        }
        if (egreso.getFromMovimientoOrigenFondosId() == null) {
            egreso.setFromMovimientoOrigenFondosId(anterior.getFromMovimientoOrigenFondosId());
        }
        resolverTipoYNaturaleza(egreso);
        validarOrigenFondos(egreso);
        validarMetodoPago(egreso);
        egreso.setId(id);
        Egreso saved = egresoRepository.save(egreso);
        movimientoOrigenFondosService.sincronizarSalidaEgreso(anterior, saved);
        ajustarEstadisticas(egreso.getFecha());
        return saved;
    }

    /**
     * Carga proveedor/tipo reales; tipo del egreso es snapshot (default = tipo del proveedor).
     * Naturaleza se infiere del tipo si no viene.
     */
    private void resolverTipoYNaturaleza(Egreso egreso) {
        if (egreso.getProveedor() == null || egreso.getProveedor().getId() == null) {
            throw new IllegalArgumentException("Debe indicar el proveedor del egreso.");
        }
        Proveedor proveedor = proveedorRepository.findById(egreso.getProveedor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado."));
        egreso.setProveedor(proveedor);

        Long tipoId = egreso.getTipoEgreso() != null ? egreso.getTipoEgreso().getId() : null;
        if (tipoId == null && proveedor.getTipoEgreso() != null) {
            tipoId = proveedor.getTipoEgreso().getId();
        }
        if (tipoId == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el tipo de egreso (o asigne un tipo al proveedor).");
        }
        TipoEgreso tipo = tipoEgresoRepository.findById(tipoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de egreso no encontrado."));
        egreso.setTipoEgreso(tipo);

        if (egreso.getNaturaleza() == null) {
            if (tipo.getNaturaleza() == null || tipo.getNaturaleza().getCodigo() == null) {
                throw new IllegalArgumentException(
                        "El tipo de egreso no tiene naturaleza asignada en el catálogo");
            }
            try {
                egreso.setNaturaleza(NaturalezaEgreso.valueOf(tipo.getNaturaleza().getCodigo().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Código de naturaleza no válido en catálogo: " + tipo.getNaturaleza().getCodigo());
            }
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Egreso existente = findById(id);
        if (existente != null) {
            LocalDate fechaEgreso = existente.getFecha();
            movimientoOrigenFondosService.revertirMovimientosEgreso(id, "Eliminación egreso #" + id);
            egresoRepository.deleteById(id);
            ajustarEstadisticas(fechaEgreso);
            return true;
        }
        return false;
    }

    private void ajustarEstadisticas(LocalDate requestDate) {
        if (requestDate == null) {
            return;
        }
        LocalDate currentDate = DateUtils.obtenerFechaSistema().toLocalDate();

        if (requestDate.isBefore(currentDate)) {
            log.info("La fecha del egreso {} es menor a la fecha actual {}. Iniciando ajuste de estadísticas.", requestDate, currentDate);

            String valorTiempoDia = requestDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            log.info("Ajustando estadística de DÍA para: {}", valorTiempoDia);
            estadisticaFinancieraService.crearOActualizarEstadisticaSync(valorTiempoDia);

            java.time.YearMonth requestMonth = java.time.YearMonth.from(requestDate);
            java.time.YearMonth currentMonth = java.time.YearMonth.from(currentDate);
            if (requestMonth.isBefore(currentMonth)) {
                String valorTiempoMes = requestMonth.toString();
                log.info("Ajustando estadística de MES para: {}", valorTiempoMes);
                estadisticaFinancieraService.crearOActualizarEstadisticaSync(valorTiempoMes);
            }

            int requestYear = requestDate.getYear();
            int currentYear = currentDate.getYear();
            if (requestYear < currentYear) {
                String valorTiempoAnio = String.valueOf(requestYear);
                log.info("Ajustando estadística de AÑO para: {}", valorTiempoAnio);
                estadisticaFinancieraService.crearOActualizarEstadisticaSync(valorTiempoAnio);
            }
        }
    }

    @Override
    public List<Egreso> findAll() {
        return egresoRepository.findAll();
    }

    @Override
    public Page<Egreso> search(String descripcion, Long tipoEgresoId, String naturaleza, Long proveedorId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        NaturalezaEgreso nat = null;
        if (naturaleza != null && !naturaleza.isBlank()) {
            try {
                nat = NaturalezaEgreso.valueOf(naturaleza.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Naturaleza de egreso no válida: " + naturaleza);
            }
        }
        return egresoRepository.search(descripcion, tipoEgresoId, nat, proveedorId, fechaInicio, fechaFin, pageable);
    }

    @Override
    public Page<Egreso> searchDescripciones(String descripcion, Pageable pageable) {
        return egresoRepository.searchDescripciones(descripcion, pageable);
    }

    @Override
    public Egreso findById(Long id) {
        return egresoRepository.findById(id).orElse(null);
    }

    @Override
    public List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return egresoRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    public List<Egreso> findByProveedorId(Long proveedorId) {
        return egresoRepository.findByProveedorId(proveedorId);
    }

    @Override
    public List<Egreso> findByTipoEgresoId(Long tipoEgresoId) {
        return egresoRepository.findByTipoEgresoId(tipoEgresoId);
    }
}
