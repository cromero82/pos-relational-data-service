package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.ProveedorRequest;
import com.infinitesoft.pos_relational_data_service.entities.Proveedor;
import com.infinitesoft.pos_relational_data_service.services.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @PostMapping
    public ResponseEntity<Proveedor> create(@Valid @RequestBody ProveedorRequest request) {
        Proveedor proveedor = Proveedor.builder()
                .documento(request.getDocumento())
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .build();
        Proveedor saved = proveedorService.create(proveedor);
        URI location = URI.create("/proveedores/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Proveedor> findAll() {
        return proveedorService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> findById(@PathVariable Long id) {
        Proveedor found = proveedorService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<Proveedor> findByDocumento(@PathVariable String documento) {
        Proveedor found = proveedorService.findByDocumento(documento);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> update(@PathVariable Long id, @RequestBody Proveedor proveedor) {
        Proveedor updated = proveedorService.update(id, proveedor);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = proveedorService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
