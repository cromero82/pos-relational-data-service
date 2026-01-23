package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductService {
    Page<Product> getAll(String barcodeOrName, Pageable pageable);
    Page<Product> getByName(String name, Pageable pageable);
    Optional<Product> getByBarcode(String barcode);
    Product create(Product product);
    Product update(Long id, Product product);
    Product deactivate(Long id);
    Product activate(Long id);
    boolean delete(Long id);
}