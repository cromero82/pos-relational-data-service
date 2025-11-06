package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialProductoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private HistorialProductoRepository historialProductoRepository;

    @Override
    public Page<Product> getAll(String barcodeOrName, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (barcodeOrName != null && !barcodeOrName.isEmpty()) {
            String q = barcodeOrName.toUpperCase();
            return productRepository.searchActiveByBarcodeOrNombreContaining(q, sorted);
        }
        return productRepository.findAllActive(sorted);
    }

    @Override
    public Page<Product> getByName(String name, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (name == null || name.isEmpty()) {
            return productRepository.findAllActive(sorted);
        }
        String n = name.toUpperCase();
        return productRepository.findActiveByNombreContaining(n, sorted);
    }

    private Pageable withNameAsc(Pageable pageable) {
        Sort sort = Sort.by(Sort.Order.asc("nombre").ignoreCase());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    @Transactional
    public Product create(Product product) {
        // Normalize fields safely (allow null barcode/nombre)
        if (product.getNombre() != null) {
            product.setNombre(product.getNombre().toUpperCase());
        }
        if (product.getBarcode() != null) {
            product.setBarcode(product.getBarcode().toUpperCase());
        }
        // Ensure activate defaults to 1 (active)
        if (product.getActivate() == null) {
            product.setActivate(1);
        }
        Product saved = productRepository.save(product);
        // Register historical record for manual creation
        HistorialProducto historial = HistorialProducto.builder()
                .productoId(saved.getId())
                .evento("manual creation")
                .precio(saved.getPrecio() != null ? BigDecimal.valueOf(saved.getPrecio()) : null)
                .activo(true)
                .build();
        historialProductoRepository.save(historial);
        return saved;
    }

    @Override
    public Product update(Long id, Product product) {
        if (id == null) {
            return null;
        }
        Optional<Product> existingOpt = productRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }
        Product existing = existingOpt.get();
        // Full replace (PUT semantics), but keep primary key (id) from path
        if (product.getBarcode() != null) {
            existing.setBarcode(product.getBarcode().toUpperCase());
        } else {
            existing.setBarcode(null);
        }
        if (product.getNombre() != null) {
            existing.setNombre(product.getNombre().toUpperCase());
        } else {
            existing.setNombre(null);
        }
        existing.setPrecio(product.getPrecio());
        existing.setFoto(product.getFoto());
        // Preserve activate flag unless explicitly provided
        if (product.getActivate() != null) {
            existing.setActivate(product.getActivate());
        }
        return productRepository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        int updated = productRepository.softDeleteById(id);
        return updated > 0;
    }
}