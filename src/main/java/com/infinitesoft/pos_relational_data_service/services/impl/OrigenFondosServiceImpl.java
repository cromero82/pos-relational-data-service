package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.CrearOrigenHijoRequest;
import com.infinitesoft.pos_relational_data_service.dto.OrigenFondosArbolItemDto;
import com.infinitesoft.pos_relational_data_service.dto.OrigenFondosDto;
import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.TipoOrigenFondos;
import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaOrigenFondos;
import com.infinitesoft.pos_relational_data_service.repositories.MovimientoOrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.OrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TipoOrigenFondosRepository;
import com.infinitesoft.pos_relational_data_service.services.OrigenFondosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrigenFondosServiceImpl implements OrigenFondosService {

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_ARCHIVADO = "ARCHIVADO";
    private static final String NOMBRE_CAJA_EFECTIVO = "Caja: Efectivo";
    private static final String NOMBRE_CAJA_EFECTIVO_ALT = "Caja Efectivo";

    @Autowired
    private OrigenFondosRepository cuentaRepository;

    @Autowired
    private MovimientoOrigenFondosRepository movimientoRepository;

    @Autowired
    private TipoOrigenFondosRepository tipoOrigenFondosRepository;

    @Override
    public List<OrigenFondosDto> findAllActivas() {
        return cuentaRepository.findByActivoTrueOrderByOrdenAscIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrigenFondosDto> findParaEgreso() {
        return cuentaRepository.findByVisibleEnEgresoTrueAndActivoTrueOrderByOrdenAscIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrigenFondosArbolItemDto> findArbol() {
        return flattenArbol(cuentaRepository.findByActivoTrueOrderByOrdenAscIdAsc(), false);
    }

    @Override
    public List<OrigenFondosArbolItemDto> findArbolParaEgreso() {
        return flattenArbol(cuentaRepository.findByActivoTrueOrderByOrdenAscIdAsc(), true);
    }

    @Override
    public OrigenFondosDto findById(Integer id) {
        return cuentaRepository.findById(id).map(this::toDto).orElse(null);
    }

    /**
     * Resuelve el método de pago asociado a la cuenta (o a un ancestro).
     * Puede ser {@code null}: hay orígenes (p. ej. Caja Menor / General) sin medio de pago;
     * el egreso/movimiento prima por {@code origenFondosId}.
     */
    @Override
    public Long resolverMetodoPagoId(Integer origenFondosId) {
        if (origenFondosId == null) {
            throw new IllegalArgumentException("La origen de fondos es obligatoria.");
        }
        OrigenFondos actual = cuentaRepository.findById(origenFondosId)
                .orElseThrow(() -> new IllegalArgumentException("Origen de fondos no encontrada."));
        Set<Integer> visitados = new HashSet<>();
        while (actual != null) {
            if (!visitados.add(actual.getId())) {
                break;
            }
            if (actual.getMetodoPagoId() != null) {
                return actual.getMetodoPagoId();
            }
            Integer parentId = actual.getParentOrigenFondosId();
            if (parentId == null) {
                break;
            }
            actual = cuentaRepository.findById(parentId).orElse(null);
        }
        return null;
    }

    @Override
    @Transactional
    public OrigenFondosDto crearHijo(CrearOrigenHijoRequest request) {
        if (request == null || request.getParentOrigenFondosId() == null) {
            throw new IllegalArgumentException("Debe indicar el origen padre.");
        }
        String nombre = request.getNombre() != null ? request.getNombre().trim() : "";
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre del fondo hijo es obligatorio.");
        }
        if (nombre.length() > 120) {
            throw new IllegalArgumentException("El nombre no puede superar 120 caracteres.");
        }

        OrigenFondos padre = cuentaRepository.findById(request.getParentOrigenFondosId())
                .orElseThrow(() -> new IllegalArgumentException("Origen padre no encontrado."));
        if (!Boolean.TRUE.equals(padre.getActivo()) || ESTADO_ARCHIVADO.equalsIgnoreCase(padre.getEstado())) {
            throw new IllegalArgumentException("No se puede crear un hijo bajo un origen archivado.");
        }
        if (esCajaEfectivo(padre)) {
            throw new IllegalArgumentException(
                    "No se permiten fondos hijos bajo «Caja: Efectivo».");
        }
        if (cuentaRepository.existsHermanoActivoConNombre(padre.getId(), nombre)) {
            throw new IllegalArgumentException(
                    "Ya existe un fondo hijo con ese nombre bajo «" + padre.getNombre() + "».");
        }

        Integer tipoId = request.getTipoOrigenFondosId();
        if (tipoId == null) {
            tipoId = padre.getTipoOrigenFondosId();
        }
        if (tipoId == null) {
            tipoId = tipoOrigenFondosRepository.findByCodigo("OTRO")
                    .map(TipoOrigenFondos::getId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tipo de origen OTRO no configurado."));
        } else if (!tipoOrigenFondosRepository.existsById(tipoId)) {
            throw new IllegalArgumentException("Tipo de origen de fondos no válido.");
        }

        Integer maxOrden = cuentaRepository.maxOrdenHijos(padre.getId());
        int orden = (maxOrden != null ? maxOrden : 0) + 1;

        OrigenFondos hijo = OrigenFondos.builder()
                .nombre(nombre)
                .tipoOrigenFondosId(tipoId)
                .parentOrigenFondosId(padre.getId())
                // No copiar metodoPagoId: debe ser único en la raíz (cierre/ventas).
                // El DTO del hijo hereda el MP del padre vía resolverMetodoPagoId.
                .metodoPagoId(null)
                .naturaleza(padre.getNaturaleza() != null
                        ? padre.getNaturaleza()
                        : NaturalezaOrigenFondos.ELECTRONICA)
                .visibleEnEgreso(Boolean.TRUE.equals(padre.getVisibleEnEgreso()))
                .requiereConciliacion(Boolean.TRUE.equals(padre.getRequiereConciliacion()))
                .activo(true)
                .estado(ESTADO_ACTIVO)
                .orden(orden)
                .color(padre.getColor())
                .build();

        OrigenFondos saved = cuentaRepository.save(hijo);
        return toDto(cuentaRepository.findById(saved.getId()).orElse(saved));
    }

    @Override
    @Transactional
    public OrigenFondosDto archivar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id de origen obligatorio.");
        }
        OrigenFondos cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Origen de fondos no encontrado."));
        if (esCajaEfectivo(cuenta)) {
            throw new IllegalArgumentException("No se puede archivar «Caja: Efectivo».");
        }
        if (ESTADO_ARCHIVADO.equalsIgnoreCase(cuenta.getEstado())
                || Boolean.FALSE.equals(cuenta.getActivo())) {
            throw new IllegalArgumentException("El origen ya está archivado.");
        }
        if (cuentaRepository.countHijosActivos(cuenta.getId()) > 0) {
            throw new IllegalArgumentException(
                    "No se puede archivar: tiene fondos hijos activos. Archívelos antes.");
        }
        BigDecimal saldo = movimientoRepository.sumImpactoByCuentaId(cuenta.getId());
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
        if (saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Solo se puede archivar un origen con saldo 0 (saldo actual: " + saldo + ").");
        }

        cuenta.setEstado(ESTADO_ARCHIVADO);
        cuenta.setActivo(false);
        OrigenFondos saved = cuentaRepository.save(cuenta);
        return toDto(saved);
    }

    private boolean esCajaEfectivo(OrigenFondos cuenta) {
        if (cuenta == null || cuenta.getNombre() == null) {
            return false;
        }
        String n = cuenta.getNombre().trim();
        return NOMBRE_CAJA_EFECTIVO.equalsIgnoreCase(n)
                || NOMBRE_CAJA_EFECTIVO_ALT.equalsIgnoreCase(n);
    }

    private List<OrigenFondosArbolItemDto> flattenArbol(List<OrigenFondos> cuentas, boolean soloVisibleEgreso) {
        Map<Integer, List<OrigenFondos>> hijosPorPadre = cuentas.stream()
                .filter(c -> c.getParentOrigenFondosId() != null)
                .collect(Collectors.groupingBy(OrigenFondos::getParentOrigenFondosId));

        hijosPorPadre.values().forEach(list ->
                list.sort(Comparator.comparing(OrigenFondos::getOrden, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(OrigenFondos::getId)));

        List<OrigenFondos> raices = cuentas.stream()
                .filter(c -> c.getParentOrigenFondosId() == null)
                .sorted(Comparator.comparing(OrigenFondos::getOrden, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(OrigenFondos::getId))
                .collect(Collectors.toList());

        List<OrigenFondosArbolItemDto> resultado = new ArrayList<>();
        for (OrigenFondos raiz : raices) {
            appendNodo(raiz, 0, hijosPorPadre, soloVisibleEgreso, resultado);
        }
        return resultado;
    }

    private void appendNodo(
            OrigenFondos nodo,
            int nivel,
            Map<Integer, List<OrigenFondos>> hijosPorPadre,
            boolean soloVisibleEgreso,
            List<OrigenFondosArbolItemDto> resultado
    ) {
        List<OrigenFondos> hijos = hijosPorPadre.getOrDefault(nodo.getId(), List.of());
        boolean incluirNodo = !soloVisibleEgreso || Boolean.TRUE.equals(nodo.getVisibleEnEgreso()) || !hijos.isEmpty();
        if (incluirNodo) {
            if (!soloVisibleEgreso || Boolean.TRUE.equals(nodo.getVisibleEnEgreso())) {
                resultado.add(toArbolItem(nodo, nivel));
            }
        }
        for (OrigenFondos hijo : hijos) {
            appendNodo(hijo, nivel + 1, hijosPorPadre, soloVisibleEgreso, resultado);
        }
    }

    private OrigenFondosArbolItemDto toArbolItem(OrigenFondos cuenta, int nivel) {
        BigDecimal saldo = movimientoRepository.sumImpactoByCuentaId(cuenta.getId());
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
        String indent = nivel > 0 ? "──── " : "";
        String tipoNombre = cuenta.getTipoOrigenFondos() != null ? cuenta.getTipoOrigenFondos().getNombre() : null;
        String tipoCodigo = cuenta.getTipoOrigenFondos() != null ? cuenta.getTipoOrigenFondos().getCodigo() : null;
        Long metodoPagoId = cuenta.getMetodoPagoId();
        if (metodoPagoId == null && cuenta.getParentOrigenFondosId() != null) {
            metodoPagoId = resolverMetodoPagoId(cuenta.getId());
        }
        return OrigenFondosArbolItemDto.builder()
                .id(cuenta.getId())
                .nombre(cuenta.getNombre())
                .nombreDisplay(indent + cuenta.getNombre())
                .nivel(nivel)
                .parentOrigenFondosId(cuenta.getParentOrigenFondosId())
                .metodoPagoId(metodoPagoId)
                .tipoOrigenFondosNombre(tipoNombre)
                .tipoOrigenFondosCodigo(tipoCodigo)
                .esRaiz(nivel == 0)
                .visibleEnEgreso(cuenta.getVisibleEnEgreso())
                .color(cuenta.getColor())
                .orden(cuenta.getOrden())
                .saldo(saldo)
                .build();
    }

    private OrigenFondosDto toDto(OrigenFondos cuenta) {
        BigDecimal saldo = movimientoRepository.sumImpactoByCuentaId(cuenta.getId());
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
        String tipoNombre = null;
        String tipoCodigo = null;
        if (cuenta.getTipoOrigenFondos() != null) {
            tipoNombre = cuenta.getTipoOrigenFondos().getNombre();
            tipoCodigo = cuenta.getTipoOrigenFondos().getCodigo();
        }
        return OrigenFondosDto.builder()
                .id(cuenta.getId())
                .nombre(cuenta.getNombre())
                .tipoOrigenFondosId(cuenta.getTipoOrigenFondosId())
                .tipoOrigenFondosNombre(tipoNombre)
                .tipoOrigenFondosCodigo(tipoCodigo)
                .proveedorId(cuenta.getProveedorId())
                .metodoPagoId(cuenta.getMetodoPagoId())
                .parentOrigenFondosId(cuenta.getParentOrigenFondosId())
                .naturaleza(cuenta.getNaturaleza() != null ? cuenta.getNaturaleza().name() : null)
                .visibleEnEgreso(cuenta.getVisibleEnEgreso())
                .requiereConciliacion(cuenta.getRequiereConciliacion())
                .activo(cuenta.getActivo())
                .estado(cuenta.getEstado())
                .orden(cuenta.getOrden())
                .color(cuenta.getColor())
                .notas(cuenta.getNotas())
                .saldo(saldo)
                .build();
    }
}
