package com.infinitesoft.pos_relational_data_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload del endpoint {@code POST /reporte-frontend}.
 *
 * <p>Reemplaza la antigua entidad JPA {@code ReporteFrontend} (tabla
 * {@code reporte_frontend} en postgres). El JSON que envía el front Angular
 * NO cambia — solo cambia dónde se persiste: ahora va a InfluxDB.
 *
 * <p>Ejemplo:
 * <pre>
 * {
 *   "url": "/apps/tickets",
 *   "actividadReciente": "...",
 *   "error": "TypeError: Cannot read properties..."
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteFrontendRequest {
    private String url;
    private String actividadReciente;
    private String error;
}
