package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.CastigoCarteraInventarioResult;
import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoConsecutivoDocumento;
import com.infinitesoft.pos_relational_data_service.repositories.*;
import com.infinitesoft.pos_relational_data_service.services.ConsecutivoDocumentoService;
import com.infinitesoft.pos_relational_data_service.services.MovimientoInventarioService;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private static final Logger log = LoggerFactory.getLogger(MovimientoInventarioServiceImpl.class);

    @Autowired
    private TipoMovimientoInventarioRepository tipoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private MovimientoInventarioDetalleRepository detalleRepository;

    @Autowired
    private InventarioKardexRepository kardexRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ConsecutivoDocumentoService consecutivoDocumentoService;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public void registrarVentaPos(
            HistorialRecibo historial,
            Long documentoVentaId,
            List<HistorialReciboDetalle> detalles,
            UUID usuarioId) {
        if (historial == null || historial.getId() == null || detalles == null || detalles.isEmpty()) {
            return;
        }
        TipoMovimientoInventario tipo = requireTipo("VENTA_POS");
        if (movimientoRepository.existsByHistorialReciboIdAndTipoMovimientoId(historial.getId(), tipo.getId())) {
            return;
        }
        registrarMovimiento(
                tipo,
                historial.getId(),
                documentoVentaId,
                null,
                null,
                null,
                detalles,
                usuarioId,
                null,
                false);
    }

    @Override
    @Transactional
    public void registrarReintegroVenta(
            HistorialRecibo historial,
            NotaAjusteDocumento nota,
            List<HistorialReciboDetalle> detalles,
            UUID usuarioId) {
        if (historial == null || historial.getId() == null || nota == null || detalles == null || detalles.isEmpty()) {
            return;
        }
        TipoMovimientoInventario tipo = requireTipo("REINTEGRO_VENTA");
        if (movimientoRepository.existsByHistorialReciboIdAndTipoMovimientoId(historial.getId(), tipo.getId())) {
            return;
        }
        registrarMovimiento(
                tipo,
                historial.getId(),
                null,
                nota.getId(),
                null,
                null,
                detalles,
                usuarioId,
                nota.getMotivoTexto(),
                false);
    }

    @Override
    @Transactional
    public void registrarCompraEgresoTrazabilidad(
            EntradaInventario entrada,
            List<EntradaInventarioDetalle> detalles,
            UUID usuarioId) {
        if (entrada == null || entrada.getId() == null || detalles == null || detalles.isEmpty()) {
            return;
        }
        TipoMovimientoInventario tipo = requireTipo("COMPRA_EGRESO");
        if (movimientoRepository.existsByEntradaInventarioIdAndTipoMovimientoId(entrada.getId(), tipo.getId())) {
            return;
        }

        String consecutivo = consecutivoDocumentoService.nextConsecutivo(TipoConsecutivoDocumento.MOV_INVENTARIO);
        int anio = Year.now().getValue();

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .consecutivo(consecutivo)
                .anio(anio)
                .tipoMovimientoId(tipo.getId())
                .estado("CONFIRMADA")
                .usuarioId(usuarioId)
                .entradaInventarioId(entrada.getId())
                .egresoId(entrada.getEgresoId() != null ? entrada.getEgresoId().longValue() : null)
                .motivoTexto("Entrada almacén confirmada")
                .build();
        movimiento = movimientoRepository.save(movimiento);

        for (EntradaInventarioDetalle linea : detalles) {
            if (linea.getProductoId() == null || linea.getCantidad() == null || linea.getCantidad() <= 0) {
                continue;
            }
            BigDecimal cantidad = BigDecimal.valueOf(linea.getCantidad());
            Product producto = productRepository.findById(linea.getProductoId()).orElse(null);
            if (producto == null) {
                continue;
            }
            BigDecimal saldo = BigDecimal.valueOf(producto.getExistencia() != null ? producto.getExistencia() : 0);

            MovimientoInventarioDetalle detalle = detalleRepository.save(MovimientoInventarioDetalle.builder()
                    .movimientoId(movimiento.getId())
                    .productoId(linea.getProductoId())
                    .cantidad(cantidad)
                    .direccionLinea("ENTRADA")
                    .motivoLinea("COMPRA_EGRESO")
                    .build());

            kardexRepository.save(InventarioKardex.builder()
                    .productoId(linea.getProductoId())
                    .movimientoDetalleId(detalle.getId())
                    .fechaHecho(movimiento.getFechaHecho())
                    .cantidadEntrada(cantidad)
                    .cantidadSalida(BigDecimal.ZERO)
                    .saldoResultante(saldo)
                    .usuarioId(usuarioId)
                    .build());
        }

        log.info("[KARDEX] COMPRA_EGRESO entradaId={} consecutivo={}", entrada.getId(), consecutivo);
    }

    @Override
    @Transactional
    public CastigoCarteraInventarioResult registrarCastigoCartera(
            Long reciboId,
            Long cuentaPorCobrarId,
            List<ReciboDetalle> detalles,
            UUID usuarioId,
            String motivoTexto) {
        if (reciboId == null || detalles == null || detalles.isEmpty()) {
            return new CastigoCarteraInventarioResult(null, BigDecimal.ZERO);
        }
        TipoMovimientoInventario tipo = requireTipo("CASTIGO_CARTERA");
        if (movimientoRepository.existsByReciboIdAndTipoMovimientoId(reciboId, tipo.getId())) {
            return movimientoRepository.findFirstByReciboIdAndTipoMovimientoId(reciboId, tipo.getId())
                    .map(m -> new CastigoCarteraInventarioResult(m.getId(), BigDecimal.ZERO))
                    .orElse(new CastigoCarteraInventarioResult(null, BigDecimal.ZERO));
        }

        String consecutivo = consecutivoDocumentoService.nextConsecutivo(TipoConsecutivoDocumento.MOV_INVENTARIO);
        int anio = Year.now().getValue();
        String motivo = motivoTexto != null && !motivoTexto.isBlank()
                ? motivoTexto
                : "Castigo cartera CxC #" + (cuentaPorCobrarId != null ? cuentaPorCobrarId : "?");

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .consecutivo(consecutivo)
                .anio(anio)
                .tipoMovimientoId(tipo.getId())
                .estado("CONFIRMADA")
                .usuarioId(usuarioId)
                .reciboId(reciboId)
                .motivoTexto(motivo)
                .build();
        movimiento = movimientoRepository.save(movimiento);

        BigDecimal valorCosto = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        for (ReciboDetalle linea : detalles) {
            if (linea.getProductoId() == null || linea.getCantidad() == null || linea.getCantidad() <= 0) {
                continue;
            }
            BigDecimal cantidad = BigDecimal.valueOf(linea.getCantidad());
            Product producto = productRepository.findById(linea.getProductoId()).orElse(null);
            if (producto == null) {
                continue;
            }

            BigDecimal costoUnit = resolverCostoUnitario(producto);
            valorCosto = valorCosto.add(costoUnit.multiply(cantidad)
                    .setScale(2, java.math.RoundingMode.HALF_UP));

            int existenciaActual = producto.getExistencia() != null ? producto.getExistencia() : 0;
            int nuevaExistencia = existenciaActual - linea.getCantidad();
            producto.setExistencia(nuevaExistencia);
            Product saved = productRepository.save(producto);
            if (saved.getBarcode() != null && !saved.getBarcode().isBlank()) {
                productService.evictProductCacheByBarcode(saved.getBarcode());
            }

            MovimientoInventarioDetalle detalle = detalleRepository.save(MovimientoInventarioDetalle.builder()
                    .movimientoId(movimiento.getId())
                    .productoId(linea.getProductoId())
                    .cantidad(cantidad)
                    .direccionLinea("SALIDA")
                    .motivoLinea("CASTIGO_CARTERA")
                    .build());

            kardexRepository.save(InventarioKardex.builder()
                    .productoId(linea.getProductoId())
                    .movimientoDetalleId(detalle.getId())
                    .fechaHecho(movimiento.getFechaHecho())
                    .cantidadSalida(cantidad)
                    .saldoResultante(BigDecimal.valueOf(nuevaExistencia))
                    .usuarioId(usuarioId)
                    .build());
        }

        log.info(
                "[KARDEX] CASTIGO_CARTERA reciboId={} cxcId={} consecutivo={} costo={}",
                reciboId, cuentaPorCobrarId, consecutivo, valorCosto);
        return new CastigoCarteraInventarioResult(movimiento.getId(), valorCosto);
    }

    /**
     * Costo unitario: precio_compra si &gt; 0; si no, ~85% del precio de venta (subtotal/cant no aplica).
     */
    private static BigDecimal resolverCostoUnitario(Product producto) {
        Double compra = producto.getPrecioCompra();
        if (compra != null && compra > 0) {
            return BigDecimal.valueOf(compra).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        Double venta = producto.getPrecio();
        if (venta != null && venta > 0) {
            return BigDecimal.valueOf(venta)
                    .multiply(BigDecimal.valueOf(0.85))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void registrarMovimiento(
            TipoMovimientoInventario tipo,
            Long historialReciboId,
            Long documentoVentaId,
            Long notaAjusteId,
            Long entradaInventarioId,
            Long egresoId,
            List<HistorialReciboDetalle> detalles,
            UUID usuarioId,
            String motivoTexto,
            boolean skipExistenciaUpdate) {

        String consecutivo = consecutivoDocumentoService.nextConsecutivo(TipoConsecutivoDocumento.MOV_INVENTARIO);
        int anio = Year.now().getValue();
        boolean esEntrada = "ENTRADA".equalsIgnoreCase(tipo.getDireccion());

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .consecutivo(consecutivo)
                .anio(anio)
                .tipoMovimientoId(tipo.getId())
                .estado("CONFIRMADA")
                .usuarioId(usuarioId)
                .historialReciboId(historialReciboId)
                .documentoVentaId(documentoVentaId)
                .notaAjusteId(notaAjusteId)
                .entradaInventarioId(entradaInventarioId)
                .egresoId(egresoId)
                .motivoTexto(motivoTexto)
                .build();
        movimiento = movimientoRepository.save(movimiento);

        for (HistorialReciboDetalle linea : detalles) {
            if (linea.getProductoId() == null || linea.getCantidad() == null || linea.getCantidad() <= 0) {
                continue;
            }
            BigDecimal cantidad = BigDecimal.valueOf(linea.getCantidad());
            Product producto = productRepository.findById(linea.getProductoId()).orElse(null);
            if (producto == null) {
                continue;
            }

            int existenciaActual = producto.getExistencia() != null ? producto.getExistencia() : 0;
            int nuevaExistencia = existenciaActual;
            if (!skipExistenciaUpdate) {
                if (esEntrada) {
                    nuevaExistencia = existenciaActual + linea.getCantidad();
                } else {
                    nuevaExistencia = existenciaActual - linea.getCantidad();
                }
                producto.setExistencia(nuevaExistencia);
                Product saved = productRepository.save(producto);
                if (saved.getBarcode() != null && !saved.getBarcode().isBlank()) {
                    productService.evictProductCacheByBarcode(saved.getBarcode());
                }
            }

            String direccionLinea = esEntrada ? "ENTRADA" : "SALIDA";
            MovimientoInventarioDetalle detalle = detalleRepository.save(MovimientoInventarioDetalle.builder()
                    .movimientoId(movimiento.getId())
                    .productoId(linea.getProductoId())
                    .cantidad(cantidad)
                    .direccionLinea(direccionLinea)
                    .motivoLinea(tipo.getCodigo())
                    .build());

            InventarioKardex kardex = InventarioKardex.builder()
                    .productoId(linea.getProductoId())
                    .movimientoDetalleId(detalle.getId())
                    .fechaHecho(movimiento.getFechaHecho())
                    .saldoResultante(BigDecimal.valueOf(nuevaExistencia))
                    .usuarioId(usuarioId)
                    .build();
            if (esEntrada) {
                kardex.setCantidadEntrada(cantidad);
            } else {
                kardex.setCantidadSalida(cantidad);
            }
            kardexRepository.save(kardex);
        }

        log.info("[KARDEX] {} historialId={} consecutivo={}", tipo.getCodigo(), historialReciboId, consecutivo);
    }

    private TipoMovimientoInventario requireTipo(String codigo) {
        return tipoRepository.findByCodigoAndActivoTrue(codigo)
                .orElseThrow(() -> new IllegalStateException(
                        "Tipo movimiento inventario no configurado: " + codigo + ". Ejecute Sprint 0 SQL."));
    }
}
