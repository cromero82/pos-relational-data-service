package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.AbonoCxcDto;
import com.infinitesoft.pos_relational_data_service.dto.AbrirCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CerrarCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CuentaPorCobrarDto;
import com.infinitesoft.pos_relational_data_service.dto.RegistrarAbonoCxcRequest;
import com.infinitesoft.pos_relational_data_service.dto.SincronizarCxCTicketRequest;
import com.infinitesoft.pos_relational_data_service.services.CuentaPorCobrarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero')")
@RequestMapping("/cuentas-por-cobrar")
@CrossOrigin(origins = "*")
public class CuentaPorCobrarController {

    @Autowired
    private CuentaPorCobrarService service;

    /**
     * Abre CxC desde un ticket abierto: actualiza cliente (teléfono obligatorio,
     * correo opcional) y crea {@code cuenta_por_cobrar}.
     */
    @PostMapping("/abrir")
    public ResponseEntity<CuentaPorCobrarDto> abrir(@RequestBody AbrirCuentaPorCobrarRequest request) {
        CuentaPorCobrarDto saved = service.abrirDesdeTicket(request);
        return ResponseEntity.created(URI.create("/cuentas-por-cobrar/" + saved.getId())).body(saved);
    }

    @GetMapping("/vigentes")
    public List<CuentaPorCobrarDto> listarVigentes() {
        return service.listarVigentes();
    }

    /** Archivados = PAGADA + ANULADA + CASTIGADA. */
    @GetMapping("/archivados")
    public List<CuentaPorCobrarDto> listarArchivados() {
        return service.listarArchivados();
    }

    @GetMapping("/por-recibo/{reciboId}")
    public ResponseEntity<CuentaPorCobrarDto> vigentePorRecibo(@PathVariable Long reciboId) {
        CuentaPorCobrarDto dto = service.findVigentePorRecibo(reciboId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/por-ticket/{ticketId}")
    public ResponseEntity<CuentaPorCobrarDto> vigentePorTicket(@PathVariable Long ticketId) {
        CuentaPorCobrarDto dto = service.findVigentePorTicket(ticketId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Sincroniza saldo/original de la CxC vigente del ticket con el nuevo total
     * (tras agregar/quitar productos). 204 si no hay CxC vigente.
     */
    @PostMapping("/por-ticket/{ticketId}/sincronizar-total")
    public ResponseEntity<CuentaPorCobrarDto> sincronizarTotal(
            @PathVariable Long ticketId,
            @RequestBody SincronizarCxCTicketRequest request
    ) {
        CuentaPorCobrarDto dto = service.sincronizarTotalTicket(ticketId, request);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/abonos")
    public ResponseEntity<AbonoCxcDto> registrarAbono(
            @PathVariable Long id,
            @RequestBody RegistrarAbonoCxcRequest request
    ) {
        AbonoCxcDto saved = service.registrarAbono(id, request);
        return ResponseEntity.created(
                URI.create("/cuentas-por-cobrar/" + id + "/abonos/" + saved.getId())
        ).body(saved);
    }

    @GetMapping("/{id}/abonos")
    public List<AbonoCxcDto> listarAbonos(@PathVariable Long id) {
        return service.listarAbonos(id);
    }

    /**
     * Anula CxC vigente solo si no hay abonos. Ticket sigue para cobro normal.
     */
    @PostMapping("/{id}/anular")
    public ResponseEntity<CuentaPorCobrarDto> anular(
            @PathVariable Long id,
            @RequestBody(required = false) CerrarCuentaPorCobrarRequest request
    ) {
        return ResponseEntity.ok(
                service.anular(id, request != null ? request : new CerrarCuentaPorCobrarRequest()));
    }

    /**
     * Castiga cartera: CASTIGADA + salida inventario a costo. Sin movimiento OF.
     */
    @PostMapping("/{id}/castigar")
    public ResponseEntity<CuentaPorCobrarDto> castigar(
            @PathVariable Long id,
            @RequestBody CerrarCuentaPorCobrarRequest request
    ) {
        return ResponseEntity.ok(service.castigar(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaPorCobrarDto> findById(@PathVariable Long id) {
        CuentaPorCobrarDto dto = service.findById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}
