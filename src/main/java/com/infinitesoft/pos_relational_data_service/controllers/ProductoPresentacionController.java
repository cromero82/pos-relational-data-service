package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.ProductoPresentacion;
import com.infinitesoft.pos_relational_data_service.services.ProductoPresentacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ProductoPresentacionController {

    @Autowired
    private ProductoPresentacionService service;

    @GetMapping("/products/{productoId}/presentaciones")
    @PreAuthorize("hasAnyRole('admin','cajero','invitado')")
    public List<ProductoPresentacion> listByProducto(
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "true") boolean soloActivos
    ) {
        return service.listByProductoId(productoId, soloActivos);
    }

    @GetMapping("/producto-presentaciones/{id}")
    @PreAuthorize("hasAnyRole('admin','cajero','invitado')")
    public ResponseEntity<ProductoPresentacion> getById(@PathVariable Long id) {
        ProductoPresentacion pp = service.findById(id);
        return pp != null ? ResponseEntity.ok(pp) : ResponseEntity.notFound().build();
    }

    @PostMapping("/products/{productoId}/presentaciones")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ProductoPresentacion> create(
            @PathVariable Long productoId,
            @RequestBody ProductoPresentacion body
    ) {
        body.setProductoId(productoId);
        ProductoPresentacion saved = service.create(body);
        return ResponseEntity.created(URI.create("/producto-presentaciones/" + saved.getId())).body(saved);
    }

    @PutMapping("/producto-presentaciones/{id}")
    @PreAuthorize("hasRole('admin')")
    public ProductoPresentacion update(@PathVariable Long id, @RequestBody ProductoPresentacion body) {
        return service.update(id, body);
    }

    @DeleteMapping("/producto-presentaciones/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /** Fuerza ensure+sync desde precio/precio_unidad del producto. */
    @PostMapping("/products/{productoId}/presentaciones/ensure")
    @PreAuthorize("hasAnyRole('admin','cajero')")
    public List<ProductoPresentacion> ensure(@PathVariable Long productoId) {
        service.ensureAndSyncFromProducto(productoId);
        return service.listByProductoId(productoId, true);
    }
}
