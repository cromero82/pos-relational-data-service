package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioRequest;
import com.infinitesoft.pos_relational_data_service.dto.EntradaInventarioDetalleRequest;
import com.infinitesoft.pos_relational_data_service.dto.EntradaInventarioEstadoResumenDto;
import com.infinitesoft.pos_relational_data_service.dto.PrecioCompraPreviewDto;
import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.entities.enums.BitacoraEvento;
import com.infinitesoft.pos_relational_data_service.entities.enums.EntradaInventarioEstado;
import com.infinitesoft.pos_relational_data_service.exception.EntradaInventarioException;
import com.infinitesoft.pos_relational_data_service.repositories.*;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.EntradaInventarioService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoInventarioService;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class EntradaInventarioServiceImpl implements EntradaInventarioService {

    @Autowired
    private EntradaInventarioRepository entradaRepository;

    @Autowired
    private EntradaInventarioDetalleRepository detalleRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private HistorialProductoRepository historialProductoRepository;

    @Autowired
    private HistorialPrecioProductoRepository historialPrecioProductoRepository;

    @Autowired
    private BitacoraUsuarioService bitacoraUsuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovimientoInventarioService movimientoInventarioService;

    @Override
    @Transactional
    public EntradaInventario obtenerOCrearPorEgreso(Long egresoId) {
        return entradaRepository.findByEgresoId(egresoId)
                .map(this::cargarDetalles)
                .orElseGet(() -> crearBorrador(egresoId));
    }

    @Override
    @Transactional(readOnly = true)
    public EntradaInventario findById(Long id) {
        EntradaInventario entrada = entradaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entrada de inventario no encontrada"));
        return cargarDetalles(entrada);
    }

    @Override
    @Transactional(readOnly = true)
    public EntradaInventario findByEgresoId(Long egresoId) {
        return entradaRepository.findByEgresoId(egresoId)
                .map(this::cargarDetalles)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntradaInventarioEstadoResumenDto> resumenPorEgresoIds(List<Long> egresoIds) {
        if (egresoIds == null || egresoIds.isEmpty()) {
            return Collections.emptyList();
        }
        return entradaRepository.findByEgresoIdIn(egresoIds).stream()
                .map(e -> EntradaInventarioEstadoResumenDto.builder()
                        .egresoId(e.getEgresoId())
                        .entradaId(e.getId())
                        .estado(e.getEstado())
                        .totalItems(e.getTotalItems())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioCompraPreviewDto previewPrecioCompra(Long productoId, BigDecimal precioCompraNuevo) {
        Product producto = productRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        return buildPreview(producto, precioCompraNuevo, null);
    }

    @Override
    @Transactional
    public EntradaInventarioDetalle agregarDetalle(Long entradaId, EntradaInventarioDetalleRequest request) {
        EntradaInventario entrada = obtenerEntradaEditable(entradaId);
        Product producto = productRepository.findById(request.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        PrecioCompraPreviewDto preview = buildPreview(producto, request.getPrecioCompra(), request.getPrecioVenta());
        EntradaInventarioDetalle detalle = mapPreviewToDetalle(entrada, producto.getId(), request, preview);
        detalle = detalleRepository.save(detalle);

        actualizarTotalItems(entrada);
        return detalle;
    }

    @Override
    @Transactional
    public EntradaInventarioDetalle actualizarDetalle(Long entradaId, Long detalleId, EntradaInventarioDetalleRequest request) {
        EntradaInventario entrada = obtenerEntradaEditable(entradaId);
        EntradaInventarioDetalle detalle = detalleRepository.findById(detalleId)
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado"));

        if (!detalle.getEntrada().getId().equals(entrada.getId())) {
            throw new EntradaInventarioException("El detalle no pertenece a esta entrada");
        }

        Product producto = productRepository.findById(request.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        PrecioCompraPreviewDto preview = buildPreview(producto, request.getPrecioCompra(), request.getPrecioVenta());
        detalle.setProductoId(producto.getId());
        detalle.setCantidad(request.getCantidad());
        applyPreviewToDetalle(detalle, request, preview);

        return detalleRepository.save(detalle);
    }

    @Override
    @Transactional
    public EntradaInventario eliminarDetalle(Long entradaId, Long detalleId) {
        EntradaInventario entrada = obtenerEntradaEditable(entradaId);
        EntradaInventarioDetalle detalle = detalleRepository.findById(detalleId)
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado"));

        if (!detalle.getEntrada().getId().equals(entrada.getId())) {
            throw new EntradaInventarioException("El detalle no pertenece a esta entrada");
        }

        detalleRepository.delete(detalle);
        actualizarTotalItems(entrada);
        return cargarDetalles(entrada);
    }

    @Override
    @Transactional
    public EntradaInventario confirmar(Long entradaId) {
        EntradaInventario entrada = obtenerEntradaEditable(entradaId);
        List<EntradaInventarioDetalle> detalles = detalleRepository.findByEntradaIdOrderByIdAsc(entradaId);

        if (detalles.isEmpty()) {
            throw new EntradaInventarioException("No hay productos en la entrada para confirmar");
        }

        for (EntradaInventarioDetalle detalle : detalles) {
            aplicarDetalleAProducto(entrada, detalle);
        }

        entrada.setEstado(EntradaInventarioEstado.CONFIRMADA);
        entrada.setFechaConfirmacion(DateUtils.obtenerFechaSistema());
        entradaRepository.save(entrada);

        try {
            movimientoInventarioService.registrarCompraEgresoTrazabilidad(
                    entrada, detalles, SecurityContextHelper.getUserId());
        } catch (Exception e) {
            log.warn("[ENTRADA-INV] Trazabilidad kardex no registrada entradaId={}: {}", entradaId, e.getMessage());
        }

        log.info("[ENTRADA-INV] Confirmada id={} egresoId={} items={}", entradaId, entrada.getEgresoId(), detalles.size());
        return cargarDetalles(entrada);
    }

    @Override
    @Transactional
    public EntradaInventario anular(Long entradaId) {
        EntradaInventario entrada = entradaRepository.findById(entradaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrada de inventario no encontrada"));

        if (entrada.getEstado() == EntradaInventarioEstado.CONFIRMADA) {
            throw new EntradaInventarioException("No se puede anular una entrada ya confirmada");
        }

        entrada.setEstado(EntradaInventarioEstado.ANULADA);
        entradaRepository.save(entrada);
        return cargarDetalles(entrada);
    }

    private EntradaInventario crearBorrador(Long egresoId) {
        if (!egresoRepository.existsById(egresoId)) {
            throw new EntityNotFoundException("Egreso no encontrado");
        }

        EntradaInventario entrada = EntradaInventario.builder()
                .egresoId(egresoId)
                .estado(EntradaInventarioEstado.BORRADOR)
                .usuarioId(SecurityContextHelper.getUserId())
                .totalItems(0)
                .build();

        return entradaRepository.save(entrada);
    }

    private EntradaInventario obtenerEntradaEditable(Long entradaId) {
        EntradaInventario entrada = entradaRepository.findById(entradaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrada de inventario no encontrada"));

        if (entrada.getEstado() != EntradaInventarioEstado.BORRADOR) {
            throw new EntradaInventarioException("Solo se pueden modificar entradas en estado BORRADOR");
        }
        return entrada;
    }

    private EntradaInventario cargarDetalles(EntradaInventario entrada) {
        List<EntradaInventarioDetalle> detalles = detalleRepository.findByEntradaIdOrderByIdAsc(entrada.getId());
        sincronizarDetalles(entrada, detalles);
        entrada.setTotalItems(detalles.size());
        egresoRepository.findById(entrada.getEgresoId()).ifPresent(entrada::setEgreso);
        return entrada;
    }

    /**
     * Hibernate no permite reemplazar la referencia de una colección con orphanRemoval;
     * hay que mutar la instancia persistente (clear + addAll).
     */
    private void sincronizarDetalles(EntradaInventario entrada, List<EntradaInventarioDetalle> detalles) {
        if (entrada.getDetalles() == null) {
            entrada.setDetalles(new ArrayList<>(detalles));
            return;
        }
        entrada.getDetalles().clear();
        entrada.getDetalles().addAll(detalles);
    }

    private void actualizarTotalItems(EntradaInventario entrada) {
        int total = detalleRepository.findByEntradaIdOrderByIdAsc(entrada.getId()).size();
        entrada.setTotalItems(total);
        entradaRepository.save(entrada);
    }

    private EntradaInventarioDetalle mapPreviewToDetalle(
            EntradaInventario entrada,
            Long productoId,
            EntradaInventarioDetalleRequest request,
            PrecioCompraPreviewDto preview) {

        EntradaInventarioDetalle detalle = EntradaInventarioDetalle.builder()
                .entrada(entrada)
                .productoId(productoId)
                .cantidad(request.getCantidad())
                .build();
        applyPreviewToDetalle(detalle, request, preview);
        return detalle;
    }

    private void applyPreviewToDetalle(
            EntradaInventarioDetalle detalle,
            EntradaInventarioDetalleRequest request,
            PrecioCompraPreviewDto preview) {
        detalle.setPrecioCompraRegistrado(request.getPrecioCompra());
        detalle.setPrecioCompraAnterior(preview.getPrecioCompraAnterior());
        detalle.setPrecioVentaActual(preview.getPrecioVentaActual());
        detalle.setPorcentajeVariacionCompra(preview.getPorcentajeVariacionCompra());
        detalle.setAlertaPrecioSubio(preview.isAlertaPrecioSubio());

        BigDecimal precioVentaNuevo = request.getPrecioVenta();
        BigDecimal precioVentaCatalogo = preview.getPrecioVentaActual();
        if (precioVentaNuevo != null
                && (precioVentaCatalogo == null
                || precioVentaNuevo.compareTo(precioVentaCatalogo) != 0)) {
            detalle.setPrecioVentaNuevo(precioVentaNuevo);
        } else {
            detalle.setPrecioVentaNuevo(null);
        }

        BigDecimal precioVentaGanancia = precioVentaNuevo != null
                ? precioVentaNuevo
                : precioVentaCatalogo;
        if (precioVentaGanancia != null
                && request.getPrecioCompra().compareTo(BigDecimal.ZERO) > 0) {
            double pct = ((precioVentaGanancia.doubleValue() - request.getPrecioCompra().doubleValue())
                    / request.getPrecioCompra().doubleValue()) * 100;
            detalle.setPorcentajeGananciaCalc((short) Math.round(pct));
        } else {
            detalle.setPorcentajeGananciaCalc(preview.getPorcentajeGanancia());
        }
    }

    private void aplicarDetalleAProducto(EntradaInventario entrada, EntradaInventarioDetalle detalle) {
        Product producto = productRepository.findById(detalle.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + detalle.getProductoId()));

        BigDecimal compraAntes = toBigDecimal(producto.getPrecioCompra());
        BigDecimal ventaAntes = toBigDecimal(producto.getPrecio());
        Short gananciaAntes = producto.getPorcentajeGanancia();

        double nuevoPrecioCompra = detalle.getPrecioCompraRegistrado().doubleValue();
        producto.setPrecioCompra(nuevoPrecioCompra);

        if (detalle.getPrecioVentaNuevo() != null) {
            producto.setPrecio(detalle.getPrecioVentaNuevo().doubleValue());
        }

        int existenciaActual = producto.getExistencia() != null ? producto.getExistencia() : 0;
        producto.setExistencia(existenciaActual + detalle.getCantidad());

        Product saved = productRepository.save(producto);

        if (saved.getBarcode() != null && !saved.getBarcode().isBlank()) {
            productService.evictProductCacheByBarcode(saved.getBarcode());
        }

        Short gananciaDespues = null;
        if (saved.getPrecio() != null && saved.getPrecioCompra() != null && saved.getPrecioCompra() > 0) {
            gananciaDespues = (short) Math.round(
                    ((saved.getPrecio() - saved.getPrecioCompra()) / saved.getPrecioCompra()) * 100
            );
            productRepository.actualizarPorcentajeGanancia(saved.getId(), gananciaDespues);
        }

        BigDecimal compraDespues = toBigDecimal(saved.getPrecioCompra());
        BigDecimal ventaDespues = toBigDecimal(saved.getPrecio());

        if (cambioPrecio(compraAntes, compraDespues) || cambioPrecio(ventaAntes, ventaDespues)) {
            UUID usuarioId = entrada.getUsuarioId() != null
                    ? entrada.getUsuarioId()
                    : SecurityContextHelper.getUserId();

            HistorialPrecioProducto historialPrecio = HistorialPrecioProducto.builder()
                    .entradaInventarioDetalleId(detalle.getId())
                    .productoId(saved.getId())
                    .usuarioId(usuarioId)
                    .precioCompra(compraDespues)
                    .precioCompraAntes(compraAntes)
                    .precioVenta(ventaDespues)
                    .precioVentaAntes(ventaAntes)
                    .porcentajeGanancia(gananciaDespues)
                    .porcentajeGananciaAntes(gananciaAntes)
                    .build();
            historialPrecioProductoRepository.save(historialPrecio);

            registrarBitacoraCambioPrecio(saved, compraAntes, ventaAntes, gananciaAntes,
                    compraDespues, ventaDespues, gananciaDespues);
        }

        HistorialProducto historial = HistorialProducto.builder()
                .productoId(saved.getId())
                .evento("entrada inventario")
                .precio(saved.getPrecio() != null ? BigDecimal.valueOf(saved.getPrecio()) : null)
                .activo(saved.getActivate() != null && saved.getActivate() != 0)
                .build();
        historialProductoRepository.save(historial);
    }

    private boolean cambioPrecio(BigDecimal antes, BigDecimal despues) {
        if (antes == null && despues == null) {
            return false;
        }
        if (antes == null || despues == null) {
            return true;
        }
        return antes.compareTo(despues) != 0;
    }

    private void registrarBitacoraCambioPrecio(
            Product producto,
            BigDecimal compraAntes,
            BigDecimal ventaAntes,
            Short gananciaAntes,
            BigDecimal compraDespues,
            BigDecimal ventaDespues,
            Short gananciaDespues) {
        try {
            String productoRef = producto.getId() + " (" + producto.getNombre() + ")";

            Map<String, Object> valorAntes = new LinkedHashMap<>();
            valorAntes.put("producto_id", productoRef);
            valorAntes.put("precio_compra", compraAntes);
            valorAntes.put("precio_venta", ventaAntes);
            valorAntes.put("porcentaje_ganancia", gananciaAntes);

            Map<String, Object> valorDespues = new LinkedHashMap<>();
            valorDespues.put("producto_id", productoRef);
            valorDespues.put("precio_compra", compraDespues);
            valorDespues.put("precio_venta", ventaDespues);
            valorDespues.put("porcentaje_ganancia", gananciaDespues);
            valorDespues.put("origen", "entrada inventario");

            BitacoraUsuarioRequest bitacoraRequest = new BitacoraUsuarioRequest();
            bitacoraRequest.setEvento(BitacoraEvento.ENTRADA_INV_PRECIO.getSigla());
            bitacoraRequest.setReferenciaId(producto.getId().intValue());
            bitacoraRequest.setValorAntes(objectMapper.writeValueAsString(valorAntes));
            bitacoraRequest.setValorDespues(objectMapper.writeValueAsString(valorDespues));
            bitacoraUsuarioService.save(bitacoraRequest);
        } catch (Exception e) {
            log.warn("[ENTRADA-INV] No se pudo registrar bitácora de precio productoId={}: {}",
                    producto.getId(), e.getMessage());
        }
    }

    private PrecioCompraPreviewDto buildPreview(
            Product producto,
            BigDecimal precioCompraNuevo,
            BigDecimal precioVentaPropuesto) {
        BigDecimal anterior = toBigDecimal(producto.getPrecioCompra());
        boolean tieneAnterior = anterior != null && anterior.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal precioVentaCatalogo = toBigDecimal(producto.getPrecio());
        BigDecimal precioVenta = precioVentaPropuesto != null ? precioVentaPropuesto : precioVentaCatalogo;

        boolean precioCompraCambio = !tieneAnterior
                || precioCompraNuevo.compareTo(anterior) != 0;
        boolean alertaPrecioSubio = tieneAnterior && precioCompraNuevo.compareTo(anterior) > 0;

        BigDecimal variacionCompra = null;
        String variacionDisplay = null;
        if (tieneAnterior) {
            variacionCompra = precioCompraNuevo.subtract(anterior)
                    .divide(anterior, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            variacionDisplay = formatVariacionDisplay(variacionCompra);
        }

        Short porcentajeGanancia = null;
        String gananciaDisplay = null;
        if (precioVenta != null && precioCompraNuevo.compareTo(BigDecimal.ZERO) > 0) {
            double pct = ((precioVenta.doubleValue() - precioCompraNuevo.doubleValue())
                    / precioCompraNuevo.doubleValue()) * 100;
            porcentajeGanancia = (short) Math.round(pct);
            gananciaDisplay = formatGananciaDisplay(pct);
        }

        return PrecioCompraPreviewDto.builder()
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .productoBarcode(producto.getBarcode())
                .precioCompraAnterior(tieneAnterior ? anterior : null)
                .precioCompraNuevo(precioCompraNuevo)
                .precioVentaActual(precioVentaCatalogo)
                .porcentajeGanancia(porcentajeGanancia)
                .porcentajeGananciaDisplay(gananciaDisplay)
                .porcentajeVariacionCompra(variacionCompra)
                .porcentajeVariacionCompraDisplay(variacionDisplay)
                .precioCompraCambio(precioCompraCambio)
                .alertaPrecioSubio(alertaPrecioSubio)
                .tienePrecioCompraAnterior(tieneAnterior)
                .build();
    }

    private BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }

    private String formatGananciaDisplay(double percent) {
        if (percent < 0) {
            return String.format("%.1f %%", percent);
        }
        if (percent < 1 && percent >= 0) {
            return "Menos del 1 %";
        }
        return String.format("%.1f %%", percent);
    }

    private String formatVariacionDisplay(BigDecimal variacion) {
        if (variacion == null) {
            return null;
        }
        double pct = variacion.doubleValue();
        String sign = pct > 0 ? "+" : "";
        return sign + String.format("%.1f %%", pct);
    }
}
