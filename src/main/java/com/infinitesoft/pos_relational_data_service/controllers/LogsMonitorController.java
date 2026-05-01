package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.monitoring.InfluxQueryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Proxy de consulta hacia InfluxDB 3 para el dashboard de monitoreo.
 *
 * <p>Expone los logs de backend y errores del frontend almacenados en
 * InfluxDB, evitando que Angular tenga que llamar directamente a Influx
 * (lo que provocaría errores de CORS en el browser).
 *
 * <p>Restricción: solo el rol {@code admin} puede consumir estos endpoints,
 * ya que exponen información sensible de diagnóstico.
 *
 * <h3>Endpoints</h3>
 * <pre>
 *   GET /api/v1/logs/backend
 *   GET /api/v1/logs/frontend
 * </pre>
 *
 * <h3>Query params (todos opcionales)</h3>
 * <pre>
 *   from  — fecha/datetime inicio  ej. 2026-04-01  o  2026-04-01T08:00:00Z
 *   to    — fecha/datetime fin     ej. 2026-04-25  o  2026-04-25T23:59:59Z
 *   limit — máximo de filas (1–500); si se omite no se aplica LIMIT
 * </pre>
 *
 * <h3>Ejemplo de respuesta (backend)</h3>
 * <pre>
 *   [
 *     { "time": "2026-04-25T14:00:00Z", "level": "ERROR",
 *       "logger": "com.infinitesoft...", "message": "..." },
 *     ...
 *   ]
 * </pre>
 */
@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/v1/logs")
@CrossOrigin(origins = "*")
public class LogsMonitorController {

    private final InfluxQueryService queryService;

    public LogsMonitorController(InfluxQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Registros del measurement {@code backend_log}.
     *
     * @param from  fecha/datetime de inicio (opcional)
     * @param to    fecha/datetime de fin    (opcional)
     * @param limit máximo de filas 1–500    (opcional)
     */
    @GetMapping(value = "/backend", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> backendLogs(
            @RequestParam(required = false) String  from,
            @RequestParam(required = false) String  to,
            @RequestParam(required = false) Integer limit) {

        String json = callService(() -> queryService.queryBackendLogs(from, to, limit));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    /**
     * Registros del measurement {@code frontend_error}.
     *
     * @param from  fecha/datetime de inicio (opcional)
     * @param to    fecha/datetime de fin    (opcional)
     * @param limit máximo de filas 1–500    (opcional)
     */
    @GetMapping(value = "/frontend", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> frontendErrors(
            @RequestParam(required = false) String  from,
            @RequestParam(required = false) String  to,
            @RequestParam(required = false) Integer limit) {

        String json = callService(() -> queryService.queryFrontendErrors(from, to, limit));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Convierte IllegalArgumentException (fechas inválidas) en 400 Bad Request. */
    private String callService(java.util.function.Supplier<String> fn) {
        try {
            return fn.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
