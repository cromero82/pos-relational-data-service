package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;
import com.infinitesoft.pos_relational_data_service.repositories.ConfiguracionAppRepository;
import com.infinitesoft.pos_relational_data_service.services.SandboxConsultaBdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Consulta SQL sandbox: allowlist = todas las tablas públicas; solo SELECT.
 * Gate: {@code sistema.sandbox.habilitarEndpointConsultaBd=true}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SandboxConsultaBdServiceImpl implements SandboxConsultaBdService {

    private static final String CONFIG_KEY = "sistema";
    private static final int DEFAULT_MAX_ROWS = 100;
    private static final int ABSOLUTE_MAX_ROWS = 500;
    private static final int QUERY_TIMEOUT_SEC = 15;

    /** Palabras prohibidas (DML/DDL/admin), no como subcadena de identificadores. */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "(?i)(?:^|[^a-z0-9_])("
                    + "insert|update|delete|merge|upsert|truncate|drop|alter|create|replace|"
                    + "grant|revoke|copy|execute|call|do\\s+\\$|set\\s+role|set\\s+session|"
                    + "pg_sleep|lo_import|lo_export|dblink|into\\s+outfile|load_file|"
                    + "vacuum|reindex|cluster|comment\\s+on|security\\s+definer"
                    + ")(?:[^a-z0-9_]|$)");

    private final JdbcTemplate jdbcTemplate;
    private final ConfiguracionAppRepository configuracionAppRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultar(String sql, Integer maxRows) {
        assertEndpointHabilitado();

        String cleaned = sanitizeSql(sql);
        assertSelectOnly(cleaned);

        int limit = normalizeMaxRows(maxRows);
        log.info("Sandbox consulta BD (maxRows={}): {}", limit, preview(cleaned));

        final List<String> columns = new ArrayList<>();
        final List<Map<String, Object>> fetched = new ArrayList<>();

        jdbcTemplate.query(
                con -> {
                    var ps = con.prepareStatement(cleaned);
                    ps.setQueryTimeout(QUERY_TIMEOUT_SEC);
                    ps.setMaxRows(limit + 1); // +1 para detectar truncado
                    return ps;
                },
                (ResultSet rs) -> {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        String label = meta.getColumnLabel(i);
                        if (label == null || label.isBlank()) {
                            label = meta.getColumnName(i);
                        }
                        columns.add(label);
                    }
                    while (rs.next() && fetched.size() < limit + 1) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object v = rs.getObject(i);
                            if (v instanceof java.sql.Timestamp) {
                                v = ((java.sql.Timestamp) v).toInstant().toString();
                            } else if (v instanceof java.sql.Date) {
                                v = v.toString();
                            } else if (v instanceof java.sql.Time) {
                                v = v.toString();
                            } else if (v != null && v.getClass().getName().startsWith("org.postgresql.")) {
                                v = v.toString();
                            }
                            row.put(columns.get(i - 1), v);
                        }
                        fetched.add(row);
                    }
                    return null;
                });

        boolean truncated = fetched.size() > limit;
        List<Map<String, Object>> rows = truncated
                ? new ArrayList<>(fetched.subList(0, limit))
                : fetched;

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("columns", columns);
        out.put("rows", rows);
        out.put("rowCount", rows.size());
        out.put("truncated", truncated);
        out.put("maxRows", limit);
        return out;
    }

    private void assertEndpointHabilitado() {
        Optional<ConfiguracionApp> cfg = configuracionAppRepository.findByKey(CONFIG_KEY);
        if (cfg.isEmpty() || cfg.get().getValue() == null || cfg.get().getValue().isBlank()) {
            throw new IllegalStateException(
                    "Consulta BD deshabilitada: falta configuracion_app.key=sistema con sandbox.habilitarEndpointConsultaBd");
        }
        try {
            JsonNode root = objectMapper.readTree(cfg.get().getValue());
            JsonNode flag = root.path("sandbox").path("habilitarEndpointConsultaBd");
            if (!flag.isBoolean() || !flag.asBoolean()) {
                throw new IllegalStateException(
                        "Consulta BD deshabilitada: sistema.sandbox.habilitarEndpointConsultaBd no está en true");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Consulta BD deshabilitada: no se pudo leer configuracion_app.sistema — " + e.getMessage(),
                    e);
        }
    }

    static String sanitizeSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql es obligatorio");
        }
        String s = sql.trim();
        // Quitar comentarios de línea simples para no ocultar DML
        s = s.replaceAll("(?m)--.*?$", " ");
        s = s.replaceAll("/\\*[\\s\\S]*?\\*/", " ");
        s = s.replaceAll("\\s+", " ").trim();
        while (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        if (s.contains(";")) {
            throw new IllegalArgumentException("Solo se permite una sentencia (sin ';' intermedios)");
        }
        return s;
    }

    static void assertSelectOnly(String sql) {
        String lower = sql.toLowerCase(Locale.ROOT).trim();
        if (!(lower.startsWith("select") || lower.startsWith("with"))) {
            throw new IllegalArgumentException("Solo se permiten consultas SELECT (o WITH … SELECT)");
        }
        if (FORBIDDEN.matcher(sql).find()) {
            throw new IllegalArgumentException(
                    "SQL rechazado: contiene operaciones no permitidas (solo lectura SELECT)");
        }
        // SELECT … INTO crea tabla / escribe — bloquear
        if (Pattern.compile("(?i)\\binto\\b").matcher(sql).find()) {
            throw new IllegalArgumentException("SELECT INTO / INTO no está permitido");
        }
    }

    private static int normalizeMaxRows(Integer maxRows) {
        if (maxRows == null || maxRows <= 0) {
            return DEFAULT_MAX_ROWS;
        }
        return Math.min(maxRows, ABSOLUTE_MAX_ROWS);
    }

    private static String preview(String sql) {
        return sql.length() <= 240 ? sql : sql.substring(0, 240) + "…";
    }
}
