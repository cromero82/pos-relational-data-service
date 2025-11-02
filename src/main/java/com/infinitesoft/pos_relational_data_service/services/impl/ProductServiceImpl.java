package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<Product> getAll(String barcodeOrName, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (barcodeOrName != null && !barcodeOrName.isEmpty()) {
            String q = barcodeOrName.toUpperCase();
            return productRepository.findByBarcodeContainingOrNombreContaining(q, q, sorted);
        }
        return productRepository.findAll(sorted);
    }

    @Override
    public Page<Product> getByName(String name, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (name == null || name.isEmpty()) {
            return productRepository.findAll(sorted);
        }
        String n = name.toUpperCase();
        return productRepository.findByNombreContaining(n, sorted);
    }

    private Pageable withNameAsc(Pageable pageable) {
        Sort sort = Sort.by(Sort.Order.asc("nombre").ignoreCase());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public Product create(Product product) {
        // Allows product.company to be null (no additional checks required)
        product.setNombre(product.getNombre().toUpperCase());
        product.setBarcode(product.getBarcode().toUpperCase());
        return productRepository.save(product);
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
        existing.setCompany(product.getCompany()); // may be null
        return productRepository.save(existing);
    }
}