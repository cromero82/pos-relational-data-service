package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Active-only finders
    @Query("select p from Product p where p.activate <> 0")
    Page<Product> findAllActive(Pageable pageable);

    @Query("select p from Product p where p.activate <> 0 and (upper(p.barcode) like concat('%', :q, '%') or upper(p.nombre) like concat('%', :q, '%'))")
    Page<Product> searchActiveByBarcodeOrNombreContaining(@Param("q") String q, Pageable pageable);

    @Query("select p from Product p where p.activate <> 0 and upper(p.nombre) like concat('%', :name, '%')")
    Page<Product> findActiveByNombreContaining(@Param("name") String name, Pageable pageable);

    Optional<Product> findByBarcode(String barcode);

    // Soft delete -> set activate = 0
    @Modifying
    @Transactional
    @Query("update Product p set p.activate = 0 where p.id = :id and p.activate <> 0")
    int softDeleteById(@Param("id") Long id);
}