package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.ProductoPresentacion;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;

import java.util.List;

public interface ProductoPresentacionService {

    List<ProductoPresentacion> listByProductoId(Long productoId, boolean soloActivos);

    ProductoPresentacion findById(Long id);

    ProductoPresentacion create(ProductoPresentacion presentacion);

    ProductoPresentacion update(Long id, ProductoPresentacion presentacion);

    void softDelete(Long id);

    /**
     * Resuelve la presentación a usar en una línea de venta y aplica snapshots
     * (presentacionId, factor, precio unitario, cantidadBase).
     */
    void applyPresentacionToDetalle(ReciboDetalle detalle);

    /** Asegura PAQUETE (+ UNIDAD si precio_unidad) y sincroniza precios desde producto. */
    void ensureAndSyncFromProducto(Long productoId);

    ProductoPresentacion resolveDefault(Long productoId);

    ProductoPresentacion resolveByCodigo(Long productoId, String codigo);
}
