package com.infinitesoft.pos_relational_data_service.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Idempotently makes sure the {@code monitor.influx.database} exists with the
 * configured retention period. Runs once at startup; tolerant to InfluxDB
 * being down (the retry job in {@link InfluxWriter} will handle later writes).
 *
 * <p>Equivalent CLI command for manual use:
 * <pre>
 *   influxdb3 create database --host http://127.0.0.1:8181 \
 *       --retention-period 90d infinito_logs
 * </pre>
 */
@Component
@Order(0)
public class InfluxBootstrap implements CommandLineRunner {

    private static final Logger log = LogManager.getLogger(InfluxBootstrap.class);

    private final InfluxProperties props;
    private final WebClient web;

    public InfluxBootstrap(InfluxProperties props,
                           @Qualifier("influxWebClient") WebClient web) {
        this.props = props;
        this.web   = web;
    }

    @Override
    public void run(String... args) {
        // 1) Quick health probe — if Influx is down, just log and continue.
        boolean healthy = false;
        try {
            web.get().uri("/health").retrieve().toBodilessEntity()
               .block(Duration.ofSeconds(3));
            healthy = true;
        } catch (Throwable t) {
            log.warn("InfluxDB at {} not reachable at startup ({}). " +
                     "Writes will be spooled to disk until it's up.",
                     props.getUrl(), t.getMessage());
        }
        if (!healthy) return;

        // 2) Create the database. 409 (already exists) is OK.
        Map<String, Object> body = new HashMap<>();
        body.put("db", props.getDatabase());
        body.put("retention_period", props.getRetention());

        try {
            web.post()
               .uri("/api/v3/configure/database")
               .bodyValue(body)
               .retrieve()
               .toBodilessEntity()
               .block(Duration.ofSeconds(5));
            log.info("InfluxDB database '{}' created with retention {}",
                     props.getDatabase(), props.getRetention());
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                log.info("InfluxDB database '{}' already exists", props.getDatabase());
            } else {
                log.warn("InfluxDB database create returned {}: {}",
                         e.getStatusCode(), e.getResponseBodyAsString());
            }
        } catch (Throwable t) {
            log.warn("InfluxDB database create failed: {}", t.getMessage());
        }
    }
}
