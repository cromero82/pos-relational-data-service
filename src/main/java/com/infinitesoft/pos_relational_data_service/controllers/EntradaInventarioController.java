package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.EntradaInventarioDetalleRequest;
import com.infinitesoft.pos_relational_data_service.dto.EntradaInventarioEstadoResumenDto;
import com.infinitesoft.pos_relational_data_service.dto.PrecioCompraPreviewDto;
import com.infinitesoft.pos_relational_data_service.entities.EntradaInventario;
import com.infinitesoft.pos_relational_data_service.entities.EntradaInventarioDetalle;
import com.infinitesoft.pos_relational_data_service.services.EntradaInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/api/entrada-inventario")
@CrossOrigin(origins = "*")
public class EntradaInventarioController {

    @Autowired
    private EntradaInventarioService entradaInventarioService;

    @PostMapping("/por-egreso/{egresoId}")
    public ResponseEntity<EntradaInventario> obtenerOCrearPorEgreso(@PathVariable Long egresoId) {
        EntradaInventario entrada = entradaInventarioService.obtenerOCrearPorEgreso(egresoId);
        URI location = URI.create("/api/entrada-inventario/" + entrada.getId());
        return ResponseEntity.created(location).body(entrada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaInventario> findById(@PathVariable Long id) {
        return ResponseEntity.ok(entradaInventarioService.findById(id));
    }

    @GetMapping("/por-egreso/{egresoId}")
    public ResponseEntity<EntradaInventario> findByEgresoId(@PathVariable Long egresoId) {
        EntradaInventario entrada = entradaInventarioService.findByEgresoId(egresoId);
        if (entrada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entrada);
    }

    @GetMapping("/resumen")
    public List<EntradaInventarioEstadoResumenDto> resumenPorEgresoIds(
            @RequestParam("egresoIds") String egresoIdsCsv) {
        List<Long> ids = Arrays.stream(egresoIdsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return entradaInventarioService.resumenPorEgresoIds(ids);
    }

    @GetMapping("/preview-precio")
    public PrecioCompraPreviewDto previewPrecioCompra(
            @RequestParam Long productoId,
            @RequestParam BigDecimal precioCompra) {
        return entradaInventarioService.previewPrecioCompra(productoId, precioCompra);
    }

    @PostMapping("/{entradaId}/detalle")
    public ResponseEntity<EntradaInventarioDetalle> agregarDetalle(
            @PathVariable Long entradaId,
            @Valid @RequestBody EntradaInventarioDetalleRequest request) {
        EntradaInventarioDetalle detalle = entradaInventarioService.agregarDetalle(entradaId, request);
        URI location = URI.create("/api/entrada-inventario/" + entradaId + "/detalle/" + detalle.getId());
        return ResponseEntity.created(location).body(detalle);
    }

    @PutMapping("/{entradaId}/detalle/{detalleId}")
    public ResponseEntity<EntradaInventarioDetalle> actualizarDetalle(
            @PathVariable Long entradaId,
            @PathVariable Long detalleId,
            @Valid @RequestBody EntradaInventarioDetalleRequest request) {
        return ResponseEntity.ok(entradaInventarioService.actualizarDetalle(entradaId, detalleId, request));
    }

    @DeleteMapping("/{entradaId}/detalle/{detalleId}")
    public ResponseEntity<EntradaInventario> eliminarDetalle(
            @PathVariable Long entradaId,
            @PathVariable Long detalleId) {
        return ResponseEntity.ok(entradaInventarioService.eliminarDetalle(entradaId, detalleId));
    }

    @PostMapping("/{entradaId}/confirmar")
    public ResponseEntity<EntradaInventario> confirmar(@PathVariable Long entradaId) {
        return ResponseEntity.ok(entradaInventarioService.confirmar(entradaId));
    }

    @PostMapping("/{entradaId}/anular")
    public ResponseEntity<EntradaInventario> anular(@PathVariable Long entradaId) {
        return ResponseEntity.ok(entradaInventarioService.anular(entradaId));
    }
}
