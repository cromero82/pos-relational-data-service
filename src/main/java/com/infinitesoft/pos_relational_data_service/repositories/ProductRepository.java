package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByBarcodeContaining(String barcode, Pageable pageable);
    Page<Product> findByBarcodeContainingOrNombreContaining(String barcode, String nombre, Pageable pageable);
    Page<Product> findByNombreContaining(String nombre, Pageable pageable);

    Optional<Product> findByBarcode(String barcode);
}