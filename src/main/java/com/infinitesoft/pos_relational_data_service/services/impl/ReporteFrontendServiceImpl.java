package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.ReporteFrontendRequest;
import com.infinitesoft.pos_relational_data_service.monitoring.InfluxWriter;
import com.infinitesoft.pos_relational_data_service.monitoring.LineProtocol;
import com.infinitesoft.pos_relational_data_service.services.ReporteFrontendService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Reemplaza la implementación anterior basada en JPA + tabla
 * {@code reporte_frontend}. Ahora cada reporte se convierte en una línea
 * de line protocol y se entrega al {@link InfluxWriter}, que la encola
 * y la envía en batches.
 *
 * <p>Tags (indexados, baja cardinalidad):
 * <ul>
 *   <li>{@code app}        - fijo, "pos-frontend"</li>
 *   <li>{@code url}        - ruta de la SPA donde ocurrio el error</li>
 *   <li>{@code error_type} - clase del error (TypeError, ReferenceError, ...)</li>
 * </ul>
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code error}      - mensaje + stack del error frontend</li>
 *   <li>{@code actividad}  - lista circular de actividad reciente</li>
 *   <li>{@code reporte_id} - UUID generado para identificar este reporte
 *                            (útil para correlacionar en logs)</li>
 * </ul>
 */
@Service
public class ReporteFrontendServiceImpl implements ReporteFrontendService {

    private static final Logger log = LogManager.getLogger(ReporteFrontendServiceImpl.class);

    private final InfluxWriter writer;

    public ReporteFrontendServiceImpl(InfluxWriter writer) {
        this.writer = writer;
    }

    @Override
    @Async
    public void registrar(ReporteFrontendRequest req) {
        if (req == null) return;

        String reporteId = UUID.randomUUID().toString();
        String url       = req.getUrl()   == null ? "(unknown)" : req.getUrl();
        String error     = req.getError() == null ? ""          : req.getError();
        String activity  = req.getActividadReciente() == null ? "" : req.getActividadReciente();

        LineProtocol.Line line = new LineProtocol.Line("frontend_error")
                .tag("app",        "pos-frontend")
                .tag("url",        LineProtocol.safeTag(url, 120))
                .tag("error_type", LineProtocol.safeTag(extractErrorType(error), 60))
                .field("error",      error)
                .field("actividad",  activity)
                .field("reporte_id", reporteId)
                .timestampMillis(System.currentTimeMillis());

        writer.enqueue(line);

        // Tambien dejamos huella en el log de la app (por consola).
        // Este log a su vez sera capturado por el InfluxLogAppender en
        // backend_log, lo cual permite correlacionar ambas measurements
        // por el reporteId.
        log.error("[REPORTE-FRONTEND] reporteId={} url={} error={}",
                reporteId, url, firstLine(error));
    }

    /** Extrae el "tipo" de error (lo que va antes del primer ':' de la primera línea). */
    private static String extractErrorType(String err) {
        if (err == null || err.isEmpty()) return "Unknown";
        int nl   = err.indexOf('\n');
        String first = nl < 0 ? err : err.substring(0, nl);
        int colon = first.indexOf(':');
        String t = colon < 0 ? first : first.substring(0, colon);
        t = t.trim();
        return t.isEmpty() ? "Unknown" : t;
    }

    private static String firstLine(String s) {
        if (s == null) return "";
        int nl = s.indexOf('\n');
        return nl < 0 ? s : s.substring(0, nl);
    }
}
