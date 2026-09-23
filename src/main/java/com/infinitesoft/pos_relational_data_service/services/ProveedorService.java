package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Proveedor;
import java.util.List;

public interface ProveedorService {
    Proveedor create(Proveedor proveedor);
    List<Proveedor> findAll();
    Proveedor findById(Long id);
    Proveedor findByDocumento(String documento);
    Proveedor findByNombre(String nombre);
    Proveedor update(Long id, Proveedor proveedor);
    boolean delete(Long id);
}
