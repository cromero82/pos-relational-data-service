package com.infinitesoft.pos_relational_data_service.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Servicio de consulta de logs almacenados en InfluxDB 3.
 *
 * <p>Reutiliza el {@code influxWebClient} ya configurado en {@link InfluxConfig}
 * (baseUrl, auth-header, timeouts). Las consultas se hacen vía
 * {@code GET /api/v3/query_sql?format=json} y devuelven un JSON array listo
 * para ser retransmitido al cliente Angular sin transformación adicional.
 *
 * <p>Filtros soportados (todos opcionales):
 * <ul>
 *   <li>{@code from} — fecha/datetime de inicio (inclusive). Formatos: {@code yyyy-MM-dd}
 *       o {@code yyyy-MM-ddTHH:mm:ssZ}. Si se pasa solo fecha, se asume {@code T00:00:00Z}.</li>
 *   <li>{@code to}   — fecha/datetime de fin (inclusive). Si se pasa solo fecha,
 *       se asume {@code T23:59:59Z}.</li>
 *   <li>{@code limit} — máximo de filas a retornar (1–500). Si es {@code null},
 *       no se añade cláusula LIMIT (el rango de fechas controla el volumen).</li>
 * </ul>
 *
 * <p>En caso de error (Influx caído, timeout, etc.) se devuelve {@code []}
 * de forma silenciosa para no romper la UI.
 */
@Service
public class InfluxQueryService {

    private static final Logger log = LogManager.getLogger(InfluxQueryService.class);
    private static final Duration QUERY_TIMEOUT = Duration.ofSeconds(10);
    private static final int      MAX_LIMIT      = 500;

    private static final DateTimeFormatter ISO_OUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final WebClient        web;
    private final InfluxProperties props;

    public InfluxQueryService(@Qualifier("influxWebClient") WebClient web,
                              InfluxProperties props) {
        this.web   = web;
        this.props = props;
    }

    // -----------------------------------------------------------------------
    // API pública
    // -----------------------------------------------------------------------

    /**
     * Registros del measurement {@code backend_log} con filtros opcionales.
     *
     * @param from  fecha/datetime de inicio (nullable)
     * @param to    fecha/datetime de fin    (nullable)
     * @param limit máximo de filas          (nullable → sin LIMIT)
     * @throws IllegalArgumentException si {@code from} o {@code to} no tienen formato válido
     */
    public String queryBackendLogs(String from, String to, Integer limit) {
        StringBuilder sql = new StringBuilder(
            "SELECT time, level, logger, message FROM backend_log");
        appendWhereClause(sql, from, to);
        sql.append(" ORDER BY time DESC");
        appendLimit(sql, limit);
        return executeQuery(sql.toString());
    }

    /**
     * Registros del measurement {@code frontend_error} con filtros opcionales.
     *
     * @param from  fecha/datetime de inicio (nullable)
     * @param to    fecha/datetime de fin    (nullable)
     * @param limit máximo de filas          (nullable → sin LIMIT)
     * @throws IllegalArgumentException si {@code from} o {@code to} no tienen formato válido
     */
    public String queryFrontendErrors(String from, String to, Integer limit) {
        StringBuilder sql = new StringBuilder(
            "SELECT time, url, error_type, error, actividad, reporte_id FROM frontend_error");
        appendWhereClause(sql, from, to);
        sql.append(" ORDER BY time DESC");
        appendLimit(sql, limit);
        return executeQuery(sql.toString());
    }

    // -----------------------------------------------------------------------
    // Construcción de SQL
    // -----------------------------------------------------------------------

    /**
     * Añade cláusula WHERE con filtros de tiempo si se especificó al menos uno.
     * Los valores se parsean y se re-formatean para evitar inyección SQL.
     */
    private void appendWhereClause(StringBuilder sql, String from, String to) {
        String fromTs = (from != null && !from.isBlank()) ? parseToTimestamp(from, false) : null;
        String toTs   = (to   != null && !to.isBlank())   ? parseToTimestamp(to,   true)  : null;

        if (fromTs == null && toTs == null) return;

        sql.append(" WHERE");
        if (fromTs != null) {
            sql.append(" time >= '").append(fromTs).append("'");
        }
        if (fromTs != null && toTs != null) {
            sql.append(" AND");
        }
        if (toTs != null) {
            sql.append(" time <= '").append(toTs).append("'");
        }
    }

    private void appendLimit(StringBuilder sql, Integer limit) {
        if (limit != null) {
            sql.append(" LIMIT ").append(Math.max(1, Math.min(limit, MAX_LIMIT)));
        }
    }

    /**
     * Convierte un string de fecha o datetime a un timestamp ISO 8601 UTC.
     *
     * <p>Formatos aceptados:
     * <ul>
     *   <li>{@code yyyy-MM-dd}               → fecha sola; {@code endOfDay=false} → T00:00:00Z,
     *                                           {@code endOfDay=true}  → T23:59:59Z</li>
     *   <li>{@code yyyy-MM-ddTHH:mm:ssZ}     → usado tal cual (normalizado a UTC)</li>
     *   <li>{@code yyyy-MM-ddTHH:mm:ss±HH:mm} → convertido a UTC</li>
     * </ul>
     *
     * @throws IllegalArgumentException si el formato no es reconocido
     */
    private String parseToTimestamp(String raw, boolean endOfDay) {
        String s = raw.trim();

        // Intento 1: solo fecha yyyy-MM-dd
        if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                LocalDate date = LocalDate.parse(s);
                OffsetDateTime odt = endOfDay
                    ? date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC)
                    : date.atStartOfDay().atOffset(ZoneOffset.UTC);
                return ISO_OUT.format(odt);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Fecha inválida: " + raw);
            }
        }

        // Intento 2: datetime completo (con o sin offset)
        try {
            OffsetDateTime odt = OffsetDateTime.parse(s).withOffsetSameInstant(ZoneOffset.UTC);
            return ISO_OUT.format(odt);
        } catch (DateTimeParseException ignored) {
            // continúa al siguiente intento
        }

        throw new IllegalArgumentException(
            "Formato de fecha no reconocido: '" + raw + "'. " +
            "Use 'yyyy-MM-dd' o 'yyyy-MM-ddTHH:mm:ssZ'.");
    }

    // -----------------------------------------------------------------------
    // HTTP
    // -----------------------------------------------------------------------

    private String executeQuery(String sql) {
        log.debug("InfluxQueryService SQL: {}", sql);
        try {
            String result = web.get()
                .uri(uri -> uri.path("/api/v3/query_sql")
                    .queryParam("db",     props.getDatabase())
                    .queryParam("q",      sql)
                    .queryParam("format", "json")
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(QUERY_TIMEOUT);

            return (result != null && !result.isBlank()) ? result : "[]";

        } catch (Throwable t) {
            log.warn("InfluxQueryService: consulta fallida — {}", t.getMessage());
            return "[]";
        }
    }
}
