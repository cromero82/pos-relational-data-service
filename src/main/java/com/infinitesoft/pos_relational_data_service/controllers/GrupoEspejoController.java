package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.GrupoEspejoDetalleResponse;
import com.infinitesoft.pos_relational_data_service.dto.GrupoEspejoRequest;
import com.infinitesoft.pos_relational_data_service.entities.GrupoEspejo;
import com.infinitesoft.pos_relational_data_service.services.GrupoEspejoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/grupos-espejo")
@CrossOrigin(origins = "*")
public class GrupoEspejoController {

    @Autowired
    private GrupoEspejoService grupoEspejoService;

    @GetMapping
    public ResponseEntity<List<GrupoEspejoDetalleResponse>> obtenerTodosConProductos(
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(grupoEspejoService.obtenerTodosConProductos(query));
    }

    @PostMapping
    public ResponseEntity<GrupoEspejo> crearGrupoEspejo(@Valid @RequestBody GrupoEspejoRequest request) {
        GrupoEspejo grupo = grupoEspejoService.crearGrupoEspejo(request);
        URI location = URI.create("/grupos-espejo/" + grupo.getId());
        return ResponseEntity.created(location).body(grupo);
    }

    @PutMapping("/{grupoEspejoId}/productos/{productoId}")
    public ResponseEntity<GrupoEspejo> agregarProducto(
            @PathVariable Long grupoEspejoId,
            @PathVariable Long productoId
    ) {
        GrupoEspejo grupo = grupoEspejoService.agregarProducto(grupoEspejoId, productoId);
        return ResponseEntity.ok(grupo);
    }

    @DeleteMapping("/{grupoEspejoId}/productos/{productoId}")
    public ResponseEntity<Void> quitarProducto(
            @PathVariable Long grupoEspejoId,
            @PathVariable Long productoId
    ) {
        grupoEspejoService.quitarProducto(grupoEspejoId, productoId);
        return ResponseEntity.noContent().build();
    }
}
