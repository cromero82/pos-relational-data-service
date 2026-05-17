package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDto;
import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboMetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.services.ClientService;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/recibos")
@CrossOrigin(origins = "*")
public class ReciboController {

    @Autowired
    private ReciboService reciboService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ReciboMetodoPagoRepository reciboMetodoPagoRepository;

    @PostMapping
    public ResponseEntity<Recibo> create(@RequestBody ReciboDto reciboDto) {
        Recibo saved = reciboService.create(reciboDto);
        URI location = URI.create("/recibos/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public List<Recibo> findAll() {
        return reciboService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReciboDto> findById(@PathVariable Long id) {
        Recibo found = reciboService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        // Resolve estado label from enum by estadoId
        String estadoLabel = null;
        ReciboEstado estadoEnum = ReciboEstado.fromId(found.getEstadoId());
        if (estadoEnum != null) {
            estadoLabel = estadoEnum.getLabel();
        }
        // Load Client details
        Client cliente = null;
        if (found.getClienteId() != null) {
            cliente = clientService.findById(found.getClienteId());
        }
        // Load lista de métodos de pago desde junction table
        List<Long> metodoPagoIds = reciboMetodoPagoRepository.findMetodoPagoIdsByReciboId(found.getId());

        ReciboDto dto = ReciboDto.builder()
                .id(found.getId())
                .clienteId(found.getClienteId())
                .cliente(cliente)
                .fechaCreacion(found.getFechaCreacion())
                .estadoId(found.getEstadoId())
                .estado(estadoLabel)
                .metodoPagoIds(metodoPagoIds)
                .sesionId(found.getSesionId())
                .total(found.getTotal())
                .montoRecibido(found.getMontoRecibido())
                .reciboIdPadre(found.getReciboPadreId())
                .build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recibo> update(@PathVariable Long id, @RequestBody ReciboDto reciboDto) {
        Recibo updated = reciboService.update(id, reciboDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = reciboService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
