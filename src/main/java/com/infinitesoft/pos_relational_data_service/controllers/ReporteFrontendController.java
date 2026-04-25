package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.ReporteFrontendRequest;
import com.infinitesoft.pos_relational_data_service.services.ReporteFrontendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/reporte-frontend")
@CrossOrigin(origins = "*")
public class ReporteFrontendController {

    private final ReporteFrontendService service;

    public ReporteFrontendController(ReporteFrontendService service) {
        this.service = service;
    }

    /**
     * Recibe el reporte de error desde el front Angular y lo encola para
     * persistir en InfluxDB (measurement {@code frontend_error}).
     *
     * <p>El contrato de entrada NO cambia respecto a la version basada en
     * postgres, asi que el front Angular no necesita cambios.
     */
    @PostMapping
    public ResponseEntity<Void> registrar(@RequestBody ReporteFrontendRequest reporte) {
        service.registrar(reporte);
        return ResponseEntity.accepted().build();
    }
}
