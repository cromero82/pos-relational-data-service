package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.services.ResetTransaccionesSandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Solo útil en sandbox: vacía transacciones y deja paramétricas.
 * Gate: {@code configuracion_app.sistema.sandbox.habilitarEndpointResetTransacciones=true}.
 */
@RestController
@RequestMapping("/sandbox")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('admin')")
@Slf4j
public class SandboxAdminController {

    private final ResetTransaccionesSandboxService resetTransaccionesSandboxService;

    @PostMapping("/reset-datos-transaccionales")
    public ResponseEntity<?> resetDatosTransaccionales() {
        try {
            Map<String, Object> result = resetTransaccionesSandboxService.resetTransacciones();
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            log.warn("reset-datos-transaccionales rechazado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "ok", false,
                    "error", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            log.error("reset-datos-transaccionales falló: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "ok", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("reset-datos-transaccionales error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "ok", false,
                    "error", e.getMessage() != null ? e.getMessage() : "Error desconocido"
            ));
        }
    }
}
