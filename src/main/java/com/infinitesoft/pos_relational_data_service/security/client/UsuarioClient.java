package com.infinitesoft.pos_relational_data_service.security.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);
    private final WebClient webClient;
    private final Duration timeout;
    private final String usuariosPath;

    public UsuarioClient(
            WebClient.Builder webClientBuilder,
            @Value("${auth.service.base-url:http://localhost:8081}") String baseUrl,
            @Value("${auth.service.timeout-ms:5000}") long timeoutMs,
            @Value("${auth.service.usuarios-path:/auth/usuarios}") String usuariosPath
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofMillis(timeoutMs);
        this.usuariosPath = usuariosPath;
    }

    public Object getUsuarios(String token) {
        try {
            return webClient.get()
                    .uri(usuariosPath)
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block(timeout);
        } catch (Exception e) {
            log.error("[UsuarioClient] Error fetching usuarios from {}: {}", usuariosPath, e.toString(), e);
            return null;
        }
    }
}
