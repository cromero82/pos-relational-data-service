package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.entities.EgresoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;
import com.infinitesoft.pos_relational_data_service.entities.MovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.Persona;
import com.infinitesoft.pos_relational_data_service.entities.Proveedor;
import com.infinitesoft.pos_relational_data_service.entities.TipoEgreso;
import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaEgreso;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoMovimientoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.repositories.OrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MovimientoOrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.PersonaRepository;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private PersonaRepository personaRepository;

    @Autowired
    private TipoEgresoRepository tipoEgresoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Normaliza líneas 1:N, valida cada origen y que la suma coincida con {@code valor}.
     * Conserva snapshot {@code origenFondosId}/{@code metodoPagoId} del primer origen.
     */
    private void normalizarYValidarOrigenes(Egreso egreso) {
        List<EgresoOrigenFondos> lineas = new ArrayList<>();
        if (egreso.getOrigenes() != null) {
            for (EgresoOrigenFondos linea : egreso.getOrigenes()) {
                if (linea != null && linea.getOrigenFondosId() != null) {
                    lineas.add(linea);
                }
            }
        }
        if (lineas.isEmpty() && egreso.getOrigenFondosId() != null) {
            lineas.add(EgresoOrigenFondos.builder()
                    .origenFondosId(egreso.getOrigenFondosId())
                    .valor(egreso.getValor())
                    .orden(0)
                    .build());
        }
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un origen del egreso.");
        }
        if (egreso.getValor() == null || egreso.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del egreso debe ser mayor a cero.");
        }

        Set<Integer> vistos = new HashSet<>();
        BigDecimal suma = BigDecimal.ZERO;
        int orden = 0;
        for (EgresoOrigenFondos linea : lineas) {
            if (!vistos.add(linea.getOrigenFondosId())) {
                throw new IllegalArgumentException(
                        "El origen de fondos no puede repetirse en el mismo egreso.");
            }
            if (linea.getValor() == null || linea.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Cada origen del egreso debe tener un valor mayor a cero.");
            }
            OrigenFondos cuenta = origenFondosRepository.findById(linea.getOrigenFondosId())
                    .orElseThrow(() -> new IllegalArgumentException("Origen de fondos no encontrado."));
            if (Boolean.FALSE.equals(cuenta.getActivo())) {
                throw new IllegalArgumentException("La origen de fondos no está activa: " + cuenta.getNombre());
            }
            if (!origenPermitidoParaEgreso(cuenta, egreso)) {
                if (esCuentaDelDueno(cuenta)) {
                    throw new IllegalArgumentException(
                            "Cuenta del dueño solo aplica con naturaleza PERSONAL/DIVIDENDOS "
                                    + "y persona marcada como dueño/propietario.");
                }
                throw new IllegalArgumentException(
                        "La cuenta «" + cuenta.getNombre() + "» no está habilitada para egresos.");
            }
            Long metodoId = origenFondosService.resolverMetodoPagoId(linea.getOrigenFondosId());
            linea.setMetodoPagoId(metodoId);
            validarMetodoPagoId(metodoId);
            linea.setEgreso(egreso);
            linea.setId(null);
            linea.setOrden(orden++);
            suma = suma.add(linea.getValor());
        }
        if (suma.compareTo(egreso.getValor()) != 0) {
            throw new IllegalArgumentException(
                    "La suma de los orígenes (" + suma + ") debe ser igual al valor del egreso ("
                            + egreso.getValor() + ").");
        }

        if (egreso.getOrigenes() == null) {
            egreso.setOrigenes(new ArrayList<>());
        } else {
            egreso.getOrigenes().clear();
        }
        egreso.getOrigenes().addAll(lineas);

        EgresoOrigenFondos primero = lineas.get(0);
        egreso.setOrigenFondosId(primero.getOrigenFondosId());
        egreso.setMetodoPagoId(primero.getMetodoPagoId());
    }

    /**
     * El método de pago es opcional (derivado del O.F. si existe).
     * Cuentas como Caja Menor no tienen medio de pago.
     */
    private void validarMetodoPagoId(Long metodoPagoId) {
        if (metodoPagoId == null) {
            return;
        }
        MetodoPago metodoPago = metodoPagoRepository.findById(metodoPagoId)
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado."));
        if (Boolean.FALSE.equals(metodoPago.getVisiblePagosEgresos())) {
            throw new IllegalArgumentException("El método de pago no está habilitado para egresos.");
        }
    }

    /**
     * visibleEnEgreso == true, o excepción Cuenta del dueño + persona dueño + PERSONAL/DIVIDENDOS.
     */
    private boolean origenPermitidoParaEgreso(OrigenFondos cuenta, Egreso egreso) {
        if (Boolean.TRUE.equals(cuenta.getVisibleEnEgreso())) {
            return true;
        }
        if (!esCuentaDelDueno(cuenta)) {
            return false;
        }
        NaturalezaEgreso nat = egreso.getNaturaleza();
        if (nat != NaturalezaEgreso.PERSONAL && nat != NaturalezaEgreso.DIVIDENDOS) {
            return false;
        }
        Persona persona = egreso.getPersona();
        return persona != null && Boolean.TRUE.equals(persona.getEsDuenoPropietario());
    }

    /**
     * Hijo bajo Dueños (tipo DUENOS con padre). La raíz Dueños no aplica.
     */
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
        if (padre == null && cuenta.getParentOrigenFondosId() != null) {
            padre = origenFondosRepository.findById(cuenta.getParentOrigenFondosId()).orElse(null);
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

    @Override
    @Transactional
    public Egreso create(Egreso egreso) {
        log.info("Iniciando servicio EgresoServiceImpl - Método: create - Egreso: {}", egreso);
        aplicarFormalizarDesdeMovimiento(egreso);
        resolverTipoYNaturaleza(egreso);
        normalizarYValidarOrigenes(egreso);

        LocalDate requestDate = egreso.getFecha();

        Egreso saved = egresoRepository.save(egreso);
        movimientoOrigenFondosService.registrarSalidaEgreso(saved);
        vincularNotificacionSiFormalizo(saved);

        ajustarEstadisticas(requestDate);

        return saved;
    }

    /**
     * Formalizar egreso: el dinero ya está en la bolsa (p.ej. Sin Clasificar).
     * Si no hay líneas de origen, usa el OF del movimiento; si hay, debe incluirlo.
     * La SALIDA_EGRESO de esa línea sale de esa bolsa (no resta de nuevo el banco).
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
        Integer ofMov = mov.getOrigenFondosId();
        if (egreso.getOrigenes() == null || egreso.getOrigenes().isEmpty()) {
            egreso.setOrigenFondosId(ofMov);
        } else {
            boolean incluido = egreso.getOrigenes().stream()
                    .anyMatch(o -> ofMov != null && ofMov.equals(o.getOrigenFondosId()));
            if (!incluido) {
                throw new IllegalArgumentException(
                        "El origen del movimiento a formalizar debe estar entre los orígenes del egreso.");
            }
        }
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
        if (egreso.getNotificacionEmailPagoId() == null) {
            egreso.setNotificacionEmailPagoId(anterior.getNotificacionEmailPagoId());
        }
        resolverTipoYNaturaleza(egreso);
        normalizarYValidarOrigenes(egreso);

        Egreso snapshotAnterior = Egreso.builder()
                .id(anterior.getId())
                .fecha(anterior.getFecha())
                .valor(anterior.getValor())
                .origenFondosId(anterior.getOrigenFondosId())
                .origenes(copiarLineas(anterior.origenesEfectivos()))
                .build();

        copiarCamposEgreso(anterior, egreso);
        reemplazarOrigenes(anterior, egreso.getOrigenes());
        Egreso saved = egresoRepository.save(anterior);
        movimientoOrigenFondosService.sincronizarSalidaEgreso(snapshotAnterior, saved);
        ajustarEstadisticas(saved.getFecha());
        return saved;
    }

    private void copiarCamposEgreso(Egreso destino, Egreso origen) {
        destino.setFecha(origen.getFecha());
        destino.setValor(origen.getValor());
        destino.setDescripcion(origen.getDescripcion());
        destino.setProveedor(origen.getProveedor());
        destino.setPersona(origen.getPersona());
        destino.setTipoEgreso(origen.getTipoEgreso());
        destino.setNaturaleza(origen.getNaturaleza());
        destino.setMetodoPagoId(origen.getMetodoPagoId());
        destino.setOrigenFondosId(origen.getOrigenFondosId());
        destino.setFromMovimientoOrigenFondosId(origen.getFromMovimientoOrigenFondosId());
        destino.setNotificacionEmailPagoId(origen.getNotificacionEmailPagoId());
    }

    private void reemplazarOrigenes(Egreso destino, List<EgresoOrigenFondos> nuevas) {
        if (destino.getOrigenes() == null) {
            destino.setOrigenes(new ArrayList<>());
        }
        destino.getOrigenes().clear();
        if (nuevas == null) {
            return;
        }
        int orden = 0;
        for (EgresoOrigenFondos linea : nuevas) {
            linea.setId(null);
            linea.setEgreso(destino);
            linea.setOrden(orden++);
            destino.getOrigenes().add(linea);
        }
    }

    private List<EgresoOrigenFondos> copiarLineas(List<EgresoOrigenFondos> origen) {
        List<EgresoOrigenFondos> copia = new ArrayList<>();
        if (origen == null) {
            return copia;
        }
        for (EgresoOrigenFondos linea : origen) {
            copia.add(EgresoOrigenFondos.builder()
                    .origenFondosId(linea.getOrigenFondosId())
                    .metodoPagoId(linea.getMetodoPagoId())
                    .valor(linea.getValor())
                    .orden(linea.getOrden())
                    .build());
        }
        return copia;
    }

    /**
     * Resuelve tipo (snapshot), naturaleza (catálogo del tipo si falta) y beneficiario XOR:
     * PERSONAL/DIVIDENDOS → persona; resto → proveedor.
     */
    private void resolverTipoYNaturaleza(Egreso egreso) {
        boolean hasPersona = egreso.getPersona() != null && egreso.getPersona().getId() != null;
        boolean hasProveedor = egreso.getProveedor() != null && egreso.getProveedor().getId() != null;
        if (hasPersona && hasProveedor) {
            throw new IllegalArgumentException(
                    "Indique proveedor o persona, no ambos.");
        }
        if (!hasPersona && !hasProveedor) {
            throw new IllegalArgumentException(
                    "Debe indicar el proveedor o la persona beneficiaria del egreso.");
        }

        Proveedor proveedor = null;
        if (hasProveedor) {
            proveedor = proveedorRepository.findById(egreso.getProveedor().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado."));
        }

        Long tipoId = egreso.getTipoEgreso() != null ? egreso.getTipoEgreso().getId() : null;
        if (tipoId == null && proveedor != null && proveedor.getTipoEgreso() != null) {
            tipoId = proveedor.getTipoEgreso().getId();
        }
        if (tipoId == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el tipo de egreso"
                            + (hasProveedor ? " (o asigne un tipo al proveedor)." : "."));
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
                egreso.setNaturaleza(NaturalezaEgreso.valueOf(
                        tipo.getNaturaleza().getCodigo().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Código de naturaleza no válido en catálogo: " + tipo.getNaturaleza().getCodigo());
            }
        }

        NaturalezaEgreso nat = egreso.getNaturaleza();
        boolean requierePersona = nat == NaturalezaEgreso.PERSONAL || nat == NaturalezaEgreso.DIVIDENDOS;
        if (requierePersona) {
            if (!hasPersona) {
                throw new IllegalArgumentException(
                        "Para naturaleza " + nat + " debe indicar la persona beneficiaria (no proveedor).");
            }
            Persona persona = personaRepository.findById(egreso.getPersona().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada."));
            if (Boolean.FALSE.equals(persona.getActivo())) {
                throw new IllegalArgumentException("La persona no está activa.");
            }
            egreso.setPersona(persona);
            egreso.setProveedor(null);
        } else {
            if (!hasProveedor) {
                throw new IllegalArgumentException(
                        "Para naturaleza " + nat + " debe indicar el proveedor.");
            }
            egreso.setProveedor(proveedor);
            egreso.setPersona(null);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Egreso existente = findById(id);
        if (existente != null) {
            LocalDate fechaEgreso = existente.getFecha();
            desvincularNotificacionDeEgreso(id);
            movimientoOrigenFondosService.revertirMovimientosEgreso(id, "Eliminación egreso #" + id);
            egresoRepository.deleteById(id);
            ajustarEstadisticas(fechaEgreso);
            return true;
        }
        return false;
    }

    /**
     * Formalizar desde movimiento de notificación: marca el correo como ASOCIADA
     * (el dinero ya está en la bolsa; no se fusiona / no se reversa).
     */
    private void vincularNotificacionSiFormalizo(Egreso saved) {
        if (saved == null || saved.getFromMovimientoOrigenFondosId() == null) {
            return;
        }
        MovimientoOrigenFondos mov = movimientoOrigenFondosRepository
                .findById(saved.getFromMovimientoOrigenFondosId())
                .orElse(null);
        if (mov == null || mov.getIdReferencia() == null) {
            return;
        }
        Long notifId = mov.getIdReferencia();
        saved.setNotificacionEmailPagoId(notifId);
        egresoRepository.save(saved);
        int updated = entityManager.createNativeQuery(
                        "UPDATE notificacion_email_pago "
                                + "SET egreso_id = :egresoId, vinculo_operacion = 'ASOCIADA' "
                                + "WHERE id = :notifId "
                                + "AND (egreso_id IS NULL OR egreso_id = :egresoId)")
                .setParameter("egresoId", saved.getId())
                .setParameter("notifId", notifId)
                .executeUpdate();
        if (updated == 0) {
            throw new IllegalArgumentException(
                    "La notificación #" + notifId + " ya está asociada a otro egreso.");
        }
        log.info("Formalizar egreso #{} ligado a notificacion #{}", saved.getId(), notifId);
    }

    private void desvincularNotificacionDeEgreso(Long egresoId) {
        entityManager.createNativeQuery(
                        "UPDATE notificacion_email_pago "
                                + "SET egreso_id = NULL, "
                                + "vinculo_operacion = CASE WHEN vinculo_operacion = 'ASOCIADA' "
                                + "THEN 'PENDIENTE' ELSE vinculo_operacion END "
                                + "WHERE egreso_id = :id")
                .setParameter("id", egresoId)
                .executeUpdate();
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
    public Page<Egreso> search(String descripcion, Long tipoEgresoId, String naturaleza, Long proveedorId,
                               Long personaId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        NaturalezaEgreso nat = null;
        if (naturaleza != null && !naturaleza.isBlank()) {
            try {
                nat = NaturalezaEgreso.valueOf(naturaleza.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Naturaleza de egreso no válida: " + naturaleza);
            }
        }
        return egresoRepository.search(descripcion, tipoEgresoId, nat, proveedorId, personaId,
                fechaInicio, fechaFin, pageable);
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
