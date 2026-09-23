package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.HistorialPrecioProducto;
import com.infinitesoft.pos_relational_data_service.services.HistorialPrecioProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/api/historial-precio-producto")
@CrossOrigin(origins = "*")
public class HistorialPrecioProductoController {

    @Autowired
    private HistorialPrecioProductoService historialPrecioProductoService;

    @GetMapping("/producto/{productoId}")
    public List<HistorialPrecioProducto> findByProductoId(@PathVariable Long productoId) {
        return historialPrecioProductoService.findByProductoId(productoId);
    }
}
