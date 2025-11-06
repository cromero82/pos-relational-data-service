package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<Product> getAll(String barcodeOrName, Pageable pageable);
    Page<Product> getByName(String name, Pageable pageable);
    Product create(Product product);
    Product update(Long id, Product product);
    boolean delete(Long id);
}