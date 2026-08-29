package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.entities.ProductoPresentacion;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductoPresentacionRepository;
import com.infinitesoft.pos_relational_data_service.services.ProductoPresentacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoPresentacionServiceImpl implements ProductoPresentacionService {

    @Autowired
    private ProductoPresentacionRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductoPresentacion> listByProductoId(Long productoId, boolean soloActivos) {
        if (productoId == null) {
            return List.of();
        }
        if (soloActivos) {
            return repository.findByProductoIdAndActivoTrueOrderByEsDefaultVentaDescIdAsc(productoId);
        }
        return repository.findByProductoIdOrderByEsDefaultVentaDescIdAsc(productoId);
    }

    @Override
    public ProductoPresentacion findById(Long id) {
        if (id == null) {
            return null;
        }
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ProductoPresentacion create(ProductoPresentacion presentacion) {
        validate(presentacion, true);
        if (Boolean.TRUE.equals(presentacion.getEsDefaultVenta())) {
            clearDefault(presentacion.getProductoId(), null);
        }
        return repository.save(presentacion);
    }

    @Override
    @Transactional
    public ProductoPresentacion update(Long id, ProductoPresentacion incoming) {
        ProductoPresentacion existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presentación no encontrada: " + id));
        if (incoming.getCodigo() != null) {
            existing.setCodigo(incoming.getCodigo().trim().toUpperCase());
        }
        if (incoming.getNombreMostrar() != null) {
            existing.setNombreMostrar(incoming.getNombreMostrar().trim());
        }
        if (incoming.getFactorABase() != null) {
            if (incoming.getFactorABase().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("factor_a_base debe ser > 0");
            }
            existing.setFactorABase(incoming.getFactorABase());
        }
        if (incoming.getPrecioVenta() != null) {
            existing.setPrecioVenta(incoming.getPrecioVenta());
        }
        if (incoming.getCodigoBarrasAlt() != null) {
            String alt = incoming.getCodigoBarrasAlt().trim();
            existing.setCodigoBarrasAlt(alt.isEmpty() ? null : alt);
        }
        if (incoming.getActivo() != null) {
            existing.setActivo(incoming.getActivo());
        }
        if (incoming.getEsDefaultVenta() != null) {
            if (Boolean.TRUE.equals(incoming.getEsDefaultVenta())) {
                clearDefault(existing.getProductoId(), existing.getId());
            }
            existing.setEsDefaultVenta(incoming.getEsDefaultVenta());
        }
        syncProductoPrecioCompat(existing);
        return repository.save(existing);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        ProductoPresentacion existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presentación no encontrada: " + id));
        if (Boolean.TRUE.equals(existing.getEsDefaultVenta())) {
            throw new IllegalArgumentException("No se puede desactivar la presentación default de venta.");
        }
        existing.setActivo(false);
        repository.save(existing);
    }

    @Override
    @Transactional
    public void applyPresentacionToDetalle(ReciboDetalle detalle) {
        if (detalle == null || detalle.getProductoId() == null) {
            return;
        }
        ensureAndSyncFromProducto(detalle.getProductoId());

        ProductoPresentacion pp = null;
        if (detalle.getPresentacionId() != null) {
            pp = repository.findById(detalle.getPresentacionId()).orElse(null);
            if (pp != null && !detalle.getProductoId().equals(pp.getProductoId())) {
                throw new IllegalArgumentException("presentacion_id no pertenece al producto_id");
            }
        }
        if (pp == null) {
            pp = resolveDefault(detalle.getProductoId());
        }
        if (pp == null) {
            return;
        }

        detalle.setPresentacionId(pp.getId());
        BigDecimal factor = pp.getFactorABase() != null ? pp.getFactorABase() : BigDecimal.ONE;
        detalle.setFactorSnapshot(factor);
        if (detalle.getPrecioUnitarioSnapshot() == null) {
            detalle.setPrecioUnitarioSnapshot(pp.getPrecioVenta());
        }
        int cant = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
        detalle.setCantidadBase(
                factor.multiply(BigDecimal.valueOf(cant)).setScale(4, RoundingMode.HALF_UP)
        );
    }

    @Override
    @Transactional
    public void ensureAndSyncFromProducto(Long productoId) {
        if (productoId == null) {
            return;
        }
        Optional<Product> opt = productRepository.findById(productoId);
        if (opt.isEmpty()) {
            return;
        }
        Product p = opt.get();

        ProductoPresentacion paquete = repository
                .findByProductoIdAndCodigoAndActivoTrue(productoId, ProductoPresentacion.CODIGO_PAQUETE)
                .orElse(null);
        if (paquete == null) {
            boolean existsInactive = repository.existsByProductoIdAndCodigo(productoId, ProductoPresentacion.CODIGO_PAQUETE);
            if (!existsInactive) {
                paquete = repository.save(ProductoPresentacion.builder()
                        .productoId(productoId)
                        .codigo(ProductoPresentacion.CODIGO_PAQUETE)
                        .nombreMostrar("Paquete / presentación")
                        .factorABase(BigDecimal.ONE)
                        .precioVenta(bd(p.getPrecio()))
                        .esDefaultVenta(true)
                        .activo(true)
                        .build());
            }
        } else {
            paquete.setPrecioVenta(bd(p.getPrecio()));
            if (!Boolean.TRUE.equals(paquete.getEsDefaultVenta())) {
                // keep prices in sync; default flag managed separately
            }
            repository.save(paquete);
        }

        boolean hasMenudeo = p.getPrecioUnidad() != null && p.getPrecioUnidad() > 0;
        if (hasMenudeo) {
            ProductoPresentacion unidad = repository
                    .findByProductoIdAndCodigoAndActivoTrue(productoId, ProductoPresentacion.CODIGO_UNIDAD)
                    .orElse(null);
            if (unidad == null) {
                if (!repository.existsByProductoIdAndCodigo(productoId, ProductoPresentacion.CODIGO_UNIDAD)) {
                    repository.save(ProductoPresentacion.builder()
                            .productoId(productoId)
                            .codigo(ProductoPresentacion.CODIGO_UNIDAD)
                            .nombreMostrar("Unidad (menudeo)")
                            .factorABase(BigDecimal.ONE)
                            .precioVenta(bd(p.getPrecioUnidad()))
                            .esDefaultVenta(false)
                            .activo(true)
                            .build());
                }
            } else {
                unidad.setPrecioVenta(bd(p.getPrecioUnidad()));
                repository.save(unidad);
            }
        }
    }

    @Override
    public ProductoPresentacion resolveDefault(Long productoId) {
        return repository.findByProductoIdAndEsDefaultVentaTrueAndActivoTrue(productoId)
                .or(() -> repository.findByProductoIdAndCodigoAndActivoTrue(
                        productoId, ProductoPresentacion.CODIGO_PAQUETE))
                .orElse(null);
    }

    @Override
    public ProductoPresentacion resolveByCodigo(Long productoId, String codigo) {
        if (productoId == null || codigo == null || codigo.isBlank()) {
            return null;
        }
        return repository.findByProductoIdAndCodigoAndActivoTrue(productoId, codigo.trim().toUpperCase())
                .orElse(null);
    }

    private void clearDefault(Long productoId, Long exceptId) {
        List<ProductoPresentacion> all = repository.findByProductoIdOrderByEsDefaultVentaDescIdAsc(productoId);
        for (ProductoPresentacion pp : all) {
            if (exceptId != null && exceptId.equals(pp.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(pp.getEsDefaultVenta())) {
                pp.setEsDefaultVenta(false);
                repository.save(pp);
            }
        }
    }

    private void validate(ProductoPresentacion p, boolean creating) {
        if (p.getProductoId() == null) {
            throw new IllegalArgumentException("producto_id es obligatorio");
        }
        if (p.getCodigo() == null || p.getCodigo().isBlank()) {
            throw new IllegalArgumentException("codigo es obligatorio");
        }
        p.setCodigo(p.getCodigo().trim().toUpperCase());
        if (p.getNombreMostrar() == null || p.getNombreMostrar().isBlank()) {
            p.setNombreMostrar(p.getCodigo());
        }
        if (p.getFactorABase() == null || p.getFactorABase().compareTo(BigDecimal.ZERO) <= 0) {
            p.setFactorABase(BigDecimal.ONE);
        }
        if (p.getPrecioVenta() == null) {
            p.setPrecioVenta(BigDecimal.ZERO);
        }
        if (creating && repository.existsByProductoIdAndCodigo(p.getProductoId(), p.getCodigo())) {
            throw new IllegalArgumentException("Ya existe presentación " + p.getCodigo() + " para el producto");
        }
    }

    /** Mantiene columnas duales de producto sincronizadas (compat FE legacy). */
    private void syncProductoPrecioCompat(ProductoPresentacion pp) {
        Optional<Product> opt = productRepository.findById(pp.getProductoId());
        if (opt.isEmpty()) {
            return;
        }
        Product product = opt.get();
        if (ProductoPresentacion.CODIGO_PAQUETE.equals(pp.getCodigo()) && pp.getPrecioVenta() != null) {
            product.setPrecio(pp.getPrecioVenta().doubleValue());
            productRepository.save(product);
        } else if (ProductoPresentacion.CODIGO_UNIDAD.equals(pp.getCodigo()) && pp.getPrecioVenta() != null) {
            product.setPrecioUnidad(pp.getPrecioVenta().doubleValue());
            productRepository.save(product);
        }
    }

    private static BigDecimal bd(Double v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
