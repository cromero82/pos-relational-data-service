package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.FilterRequest;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    Page<Product> getAll(String barcodeOrName, Pageable pageable);
    Page<Product> search(String query, Pageable pageable);
    Page<Product> busquedaSmart(String query, Pageable pageable, boolean unicamenteActivos);
    Page<Product> getByName(String name, Pageable pageable);
    Page<Product> busquedaPorFiltros(List<FilterRequest> filtros, Pageable pageable, String campoOrdenamiento, String orden, String query);
    Optional<Product> getByBarcode(String barcode);
    void evictProductCacheByBarcode(String barcode);
    Optional<Product> getById(Long id);
    java.util.List<Product> findAll();
    Product create(Product product);
    Product update(Long id, Product product);
    Product deactivate(Long id);
    Product activate(Long id);
    boolean delete(Long id);
}