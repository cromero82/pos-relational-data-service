package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.MovimientoTrasladoRequest;
import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.repositories.*;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboElectronicoService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class HistorialReciboElectronicoServiceImpl implements HistorialReciboElectronicoService {

    @Autowired
    private HistorialReciboElectronicoRepository hreRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private HistorialReciboRepository historialReciboRepository;

    @Autowired
    private HistorialReciboPagoRepository historialReciboPagoRepository;

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private AbonoCxcRepository abonoCxcRepository;

    @Autowired
    private OrigenFondosRepository origenFondosRepository;

    @Autowired
    private MovimientoOrigenFondosService movimientoOrigenFondosService;

    @Override
    @Transactional
    public HistorialReciboElectronico corregirMetodoPago(Long historialElectronicoId, Long nuevoMetodoPagoId) {
        if (historialElectronicoId == null) {
            throw new IllegalArgumentException("historialElectronicoId es obligatorio");
        }
        if (nuevoMetodoPagoId == null) {
            throw new IllegalArgumentException("metodoPagoId es obligatorio");
        }

        HistorialReciboElectronico hre = hreRepository.findById(historialElectronicoId)
                .orElseThrow(() -> new IllegalArgumentException("Pendiente electrónico no encontrado"));

        String estado = hre.getEstado() != null ? hre.getEstado().trim().toUpperCase() : "";
        if (!"CREADA".equals(estado) && !"AMBIGUA".equals(estado)) {
            throw new IllegalArgumentException(
                    "Solo se puede corregir el medio en pendientes CREADA o AMBIGUA (actual: " + hre.getEstado() + ")");
        }

        Long anterior = hre.getMetodoPagoId();
        if (Objects.equals(anterior, nuevoMetodoPagoId)) {
            return hre;
        }

        MetodoPago mpNuevo = metodoPagoRepository.findById(nuevoMetodoPagoId)
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado: " + nuevoMetodoPagoId));
        if (!Boolean.TRUE.equals(mpNuevo.getPermiteNotificacion())) {
            throw new IllegalArgumentException(
                    "El método «" + mpNuevo.getDescripcion() + "» no permite notificaciones electrónicas");
        }

        OrigenFondos ofNuevo = origenFondosRepository.findByMetodoPagoId(nuevoMetodoPagoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay origen de fondos raíz para el método «" + mpNuevo.getDescripcion() + "»"));

        if (hre.getHistorialReciboId() != null) {
            corregirVenta(hre, anterior, nuevoMetodoPagoId);
        } else if (hre.getAbonoCxcId() != null) {
            corregirAbono(hre, anterior, nuevoMetodoPagoId, ofNuevo);
        } else {
            throw new IllegalArgumentException("Pendiente sin venta ni abono CxC asociado");
        }

        hre.setMetodoPagoId(nuevoMetodoPagoId);
        return hreRepository.save(hre);
    }

    private void corregirVenta(HistorialReciboElectronico hre, Long metodoAnterior, Long metodoNuevo) {
        Long hrId = hre.getHistorialReciboId();
        List<HistorialReciboPago> lineas =
                historialReciboPagoRepository.findByHistorialReciboIdOrderByOrdenAsc(hrId);

        boolean actualizoLinea = false;
        for (HistorialReciboPago linea : lineas) {
            if (Objects.equals(linea.getMetodoPagoId(), metodoAnterior)
                    || (metodoAnterior == null && Objects.equals(linea.getMonto(), hre.getMontoEsperado()))) {
                linea.setMetodoPagoId(metodoNuevo);
                historialReciboPagoRepository.save(linea);
                actualizoLinea = true;
            }
        }
        if (!actualizoLinea && !lineas.isEmpty() && hre.getMontoEsperado() != null) {
            // Fallback: línea con mismo monto esperado
            for (HistorialReciboPago linea : lineas) {
                if (linea.getMonto() != null && linea.getMonto().compareTo(hre.getMontoEsperado()) == 0) {
                    linea.setMetodoPagoId(metodoNuevo);
                    historialReciboPagoRepository.save(linea);
                    actualizoLinea = true;
                    break;
                }
            }
        }
        if (!actualizoLinea) {
            throw new IllegalArgumentException(
                    "No se encontró línea de pago a corregir en historial_recibo_pago #" + hrId);
        }

        List<HistorialReciboPago> lineasActualizadas =
                historialReciboPagoRepository.findByHistorialReciboIdOrderByOrdenAsc(hrId);
        Long primario = lineasActualizadas.stream()
                .max(Comparator
                        .comparing(HistorialReciboPago::getMonto)
                        .thenComparing(l -> Long.valueOf(1).equals(l.getMetodoPagoId()) ? 1 : 0))
                .map(HistorialReciboPago::getMetodoPagoId)
                .orElse(metodoNuevo);

        historialReciboRepository.findById(hrId).ifPresent(hr -> {
            hr.setMetodoPagoId(primario);
            historialReciboRepository.save(hr);
        });

        documentoVentaRepository.findByHistorialReciboId(hrId).ifPresent(doc -> {
            doc.setMetodoPagoId(primario);
            documentoVentaRepository.save(doc);
        });

        // Venta: el ledger ENTRADA_VENTA se genera en el corte desde historial_recibo_pago.
        // No hay MOF al estado CREADA; no se hace traslado aquí.
    }

    private void corregirAbono(
            HistorialReciboElectronico hre,
            Long metodoAnterior,
            Long metodoNuevo,
            OrigenFondos ofNuevo) {
        AbonoCxc abono = abonoCxcRepository.findById(hre.getAbonoCxcId())
                .orElseThrow(() -> new IllegalArgumentException("Abono CxC no encontrado: " + hre.getAbonoCxcId()));

        Integer ofAnteriorId = abono.getOrigenFondosId();
        if (ofAnteriorId == null && metodoAnterior != null) {
            ofAnteriorId = origenFondosRepository.findByMetodoPagoId(metodoAnterior)
                    .map(OrigenFondos::getId)
                    .orElse(null);
        }

        abono.setMetodoPagoId(metodoNuevo);
        abono.setOrigenFondosId(ofNuevo.getId());
        abonoCxcRepository.save(abono);

        BigDecimal monto = hre.getMontoEsperado() != null ? hre.getMontoEsperado() : abono.getMonto();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (ofAnteriorId == null || ofAnteriorId.equals(ofNuevo.getId())) {
            return;
        }

        // Dinero ya entró vía ENTRADA_COBRANZA → traslado al OF del medio correcto
        movimientoOrigenFondosService.registrarTraslado(MovimientoTrasladoRequest.builder()
                .origenFondosId(ofAnteriorId)
                .origenDestinoId(ofNuevo.getId())
                .valor(monto)
                .observacion("Corrección medio pago HRE #" + hre.getId() + " abono CxC #" + abono.getId())
                .build());
    }
}
