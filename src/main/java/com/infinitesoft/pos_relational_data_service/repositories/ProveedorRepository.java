package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByDocumento(String documento);
    Optional<Proveedor> findFirstByNombreIgnoreCase(String nombre);
}
