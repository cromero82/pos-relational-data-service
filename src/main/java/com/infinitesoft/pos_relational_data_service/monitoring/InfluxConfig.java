package com.infinitesoft.pos_relational_data_service.monitoring;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient pre-configured to talk to the InfluxDB 3 HTTP API.
 * Used by {@link InfluxWriter} and {@link InfluxBootstrap}.
 * <p>
 * The {@code Authorization} header is added per-request only when
 * {@code monitor.influx.auth-header} is non-empty.
 */
@Configuration
public class InfluxConfig {

    @Bean(name = "influxWebClient")
    public WebClient influxWebClient(InfluxProperties props) {
        long timeoutMs = props.getHttpTimeoutMs();
        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeoutMs)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)));

        WebClient.Builder b = WebClient.builder()
                .baseUrl(props.getUrl())
                .clientConnector(new ReactorClientHttpConnector(http))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);

        if (props.getAuthHeader() != null && !props.getAuthHeader().isEmpty()) {
            b.defaultHeader("Authorization", props.getAuthHeader());
        }
        return b.build();
    }
}
