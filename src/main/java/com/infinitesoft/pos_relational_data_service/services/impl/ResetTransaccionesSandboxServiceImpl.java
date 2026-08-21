package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;
import com.infinitesoft.pos_relational_data_service.repositories.ConfiguracionAppRepository;
import com.infinitesoft.pos_relational_data_service.services.ResetTransaccionesSandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Equivalente al SQL v3, sin ScriptUtils (rompe en bloques {@code DO $$}).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResetTransaccionesSandboxServiceImpl implements ResetTransaccionesSandboxService {

    private static final String CONFIG_KEY = "sistema";
    private static final String VERSION = "v3-jdbc";

    /** Mismo orden/candidatas que reset-tablas-financieras-transaccionales-v3.sql */
    private static final List<String> TABLAS_TRANSACCIONALES = Arrays.asList(
            "abono_cxc",
            "cuenta_por_cobrar",
            "corte_venta_detalle",
            "ventas_tipo",
            "corte_venta",
            "movimiento_origen_fondos",
            "movimiento_bolsillo",
            "egreso",
            "entrada_inventario_detalle",
            "entrada_inventario",
            "movimiento_inventario_detalle",
            "movimiento_inventario",
            "inventario_kardex",
            "nota_ajuste_detalle",
            "nota_ajuste_documento",
            "edicion_recibo_detalle",
            "edicion_recibo",
            "ticket_sin_notificacion",
            "notificacion_email_pago",
            "historial_recibos_electronicos",
            "ticket_recibo",
            "historial_recibo_pago",
            "historial_recibo_detalle",
            "recibo_detalle_historico",
            "recibo_detalle",
            "documento_venta",
            "historial_recibo",
            "recibo",
            "ticket",
            "consecutivo_documento",
            "estadistica_fin",
            "flujo_dinero",
            "cargue_producto_conflictos",
            "cargue_productos",
            "historial_precio_producto",
            "historial_producto"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ConfiguracionAppRepository configuracionAppRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Map<String, Object> resetTransacciones() {
        assertEndpointHabilitado();

        List<String> existentes = tablasExistentes(TABLAS_TRANSACCIONALES);
        if (existentes.isEmpty()) {
            throw new IllegalArgumentException("Ninguna tabla candidata existe; abortando reset.");
        }

        String truncateSql = "TRUNCATE TABLE "
                + existentes.stream().map(t -> "\"" + t + "\"").collect(Collectors.joining(", "))
                + " RESTART IDENTITY CASCADE";

        log.warn("Sandbox: TRUNCATE {} tablas transaccionales ({})", existentes.size(), VERSION);
        try {
            jdbcTemplate.execute(truncateSql);
        } catch (Exception e) {
            log.error("Sandbox: falló TRUNCATE: {}", e.getMessage(), e);
            throw new IllegalArgumentException("No se pudo ejecutar el reset transaccional: " + e.getMessage(), e);
        }

        assertLedgerLimpio();

        Map<String, Object> conteos = leerConteosVerificacion();
        log.warn("Sandbox: reset OK. conteos={}", conteos);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("mensaje",
                "Datos transaccionales vaciados. Cierre sesión y vuelva a entrar como ADMIN para registrar la base inicial.");
        out.put("version", VERSION);
        out.put("tablasTruncadas", existentes.size());
        out.put("conteos", conteos);
        return out;
    }

    private void assertEndpointHabilitado() {
        Optional<ConfiguracionApp> cfg = configuracionAppRepository.findByKey(CONFIG_KEY);
        if (cfg.isEmpty() || cfg.get().getValue() == null || cfg.get().getValue().isBlank()) {
            throw new IllegalStateException(
                    "Reset deshabilitado: falta configuracion_app.key=sistema con sandbox.habilitarEndpointResetTransacciones");
        }
        try {
            JsonNode root = objectMapper.readTree(cfg.get().getValue());
            JsonNode flag = root.path("sandbox").path("habilitarEndpointResetTransacciones");
            if (!flag.isBoolean() || !flag.asBoolean()) {
                throw new IllegalStateException(
                        "Reset deshabilitado: sistema.sandbox.habilitarEndpointResetTransacciones no está en true");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Reset deshabilitado: no se pudo leer configuracion_app.sistema — " + e.getMessage(), e);
        }
    }

    private List<String> tablasExistentes(List<String> candidatas) {
        List<String> out = new ArrayList<>();
        for (String t : candidatas) {
            Integer n = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema = 'public' AND table_name = ? AND table_type = 'BASE TABLE'",
                    Integer.class,
                    t);
            if (n != null && n > 0) {
                out.add(t);
            } else {
                log.info("Sandbox reset: omitida (no existe): {}", t);
            }
        }
        return out;
    }

    private void assertLedgerLimpio() {
        Long nBase = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM movimiento_origen_fondos WHERE origen_tipo = 'BASE_INICIAL'",
                Long.class);
        Long nCorte = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM corte_venta WHERE estado <> 'eliminado'",
                Long.class);
        Long nHr = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM historial_recibo", Long.class);
        Long nMof = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM movimiento_origen_fondos", Long.class);
        if ((nBase != null && nBase > 0)
                || (nCorte != null && nCorte > 0)
                || (nHr != null && nHr > 0)
                || (nMof != null && nMof > 0)) {
            throw new IllegalArgumentException(String.format(
                    "Reset incompleto: BASE_INICIAL=%s, cortes_activos=%s, historial_recibo=%s, MOF=%s",
                    nBase, nCorte, nHr, nMof));
        }
    }

    private Map<String, Object> leerConteosVerificacion() {
        Map<String, Object> m = new LinkedHashMap<>();
        String sql =
                "SELECT 'corte_venta' AS t, COUNT(*)::bigint AS n FROM corte_venta "
                        + "UNION ALL SELECT 'movimiento_origen_fondos', COUNT(*) FROM movimiento_origen_fondos "
                        + "UNION ALL SELECT 'historial_recibo', COUNT(*) FROM historial_recibo "
                        + "UNION ALL SELECT 'egreso', COUNT(*) FROM egreso "
                        + "UNION ALL SELECT 'notificacion_email_pago', COUNT(*) FROM notificacion_email_pago "
                        + "UNION ALL SELECT 'cuenta_por_cobrar', COUNT(*) FROM cuenta_por_cobrar "
                        + "UNION ALL SELECT 'origen_fondos', COUNT(*) FROM origen_fondos "
                        + "UNION ALL SELECT 'metodo_pago', COUNT(*) FROM metodo_pago "
                        + "UNION ALL SELECT 'producto', COUNT(*) FROM producto";
        try {
            jdbcTemplate.query(sql, rs -> {
                m.put(rs.getString(1), rs.getLong(2));
            });
        } catch (Exception e) {
            log.warn("No se pudieron leer conteos post-reset: {}", e.getMessage());
            m.put("aviso", "reset ejecutado; conteos no disponibles: " + e.getMessage());
        }
        return m;
    }
}
