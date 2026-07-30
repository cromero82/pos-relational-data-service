package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.HistorialDocumentosDto;
import com.infinitesoft.pos_relational_data_service.dto.MotivoOperacionRequestDto;
import com.infinitesoft.pos_relational_data_service.dto.RestaurarTicketResponseDto;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/historial-recibos")
@CrossOrigin(origins = "*")
public class HistorialReciboController {

    @Autowired
    private HistorialReciboService service;

    @PostMapping
    public ResponseEntity<HistorialRecibo> create(@RequestBody HistorialRecibo historialRecibo) {
        HistorialRecibo saved = service.create(historialRecibo);
        URI location = URI.create("/historial-recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // Special endpoint: quick add with default estado = PAGADO
    @PostMapping("/addquickRecibo")
    public ResponseEntity<HistorialRecibo> addquickRecibo(@RequestBody HistorialRecibo historialRecibo) {
        HistorialRecibo saved = service.createQuick(historialRecibo);
        URI location = URI.create("/historial-recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<HistorialRecibo> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialRecibo> findById(@PathVariable Long id) {
        HistorialRecibo found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialRecibo> update(@PathVariable Long id, @RequestBody HistorialRecibo historialRecibo, @RequestParam(required = true) Long sesionId) {
        HistorialRecibo updated = service.update(id, historialRecibo, sesionId);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/documentos")
    public ResponseEntity<HistorialDocumentosDto> getDocumentos(@PathVariable Long id) {
        HistorialDocumentosDto dto = service.getDocumentos(id);
        boolean vacio = dto.getDocumentoVenta() == null
                && (dto.getNotasAjuste() == null || dto.getNotasAjuste().isEmpty());
        if (vacio) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/restaurar-ticket")
    @PreAuthorize("hasAnyRole('admin','cajero')")
    public ResponseEntity<RestaurarTicketResponseDto> restaurarTicket(
            @PathVariable Long id,
            @RequestParam Long sesionId,
            @RequestBody(required = false) MotivoOperacionRequestDto body) {
        try {
            return ResponseEntity.ok(service.restaurarTicket(id, sesionId, body));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-by-date")
    public BigDecimal getTotalByDate(@RequestParam("fecha") String fecha) {
        return service.getTotalByDate(fecha);
    }

    @GetMapping("/search")
    public Page<HistorialRecibo> search(@RequestParam(value = "fecha", required = false) String fecha,
                                        @RequestParam(defaultValue = "0") Long estadoId,
                                        @RequestParam(value = "sesionId", required = false) Long sesionId,
                                        @RequestParam(value = "restaurados", required = false) Boolean restaurados,
                                        @PageableDefault(sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.search(fecha, estadoId, sesionId, restaurados, pageable);
    }
}
