package com.infinitesoft.pos_relational_data_service.security.client;

import com.infinitesoft.pos_relational_data_service.security.dto.AuthBackupDto;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthClient {

    private static final Logger log = LoggerFactory.getLogger(AuthClient.class);
    private final WebClient webClient;
    private final Duration timeout;

    private final String validatePath;
    private final String claimsPath;
    private final String userIdPath;
    private final String usuariosPath;
    private final String usuarioPath;
    private final String isExpiredPath;
    private final WebClient backupWebClient;
    private final String backupPath;

    public AuthClient(
            WebClient.Builder webClientBuilder,
            @Value("${auth.service.host:http://localhost:8081}") String serviceHost,
            @Value("${auth.service.base-path:/auth}") String basePath,
            @Value("${auth.service.timeout-ms:5000}") long timeoutMs,
            @Value("${auth.service.path.validate:/validate}") String validatePath,
            @Value("${auth.service.path.claims:/claims}") String claimsPath,
            @Value("${auth.service.path.user-id:/usuario-id}") String userIdPath,
            @Value("${auth.service.path.usuarios:/usuarios}") String usuariosPath,
            @Value("${auth.service.path.usuario:/usuario}") String usuarioPath,
            @Value("${auth.service.path.is-expired:/is-expired}") String isExpiredPath,
            @Value("${auth.service.path.backup:/backup}") String backupPath
    ) {
        String baseUrl = serviceHost + basePath;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.backupWebClient = webClientBuilder.baseUrl(serviceHost).build();
        this.timeout = Duration.ofMillis(timeoutMs);
        this.validatePath = validatePath;
        this.claimsPath = claimsPath;
        this.userIdPath = userIdPath;
        this.usuariosPath = usuariosPath;
        this.usuarioPath = usuarioPath;
        this.isExpiredPath = isExpiredPath;
        this.backupPath = backupPath;
    }

    public Boolean validateToken(String token) {
        try {
            return webClient.post()
                    .uri(validatePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of("token", token)))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block(timeout);
        } catch (Exception e) {
            log.error("[AuthClient] Error validating token: {}", e.toString());
            return false;
        }
    }

    public AuthUserDto getClaims(String token) {
        try {
            return webClient.post()
                    .uri(claimsPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of("token", token)))
                    .retrieve()
                    .bodyToMono(AuthUserDto.class)
                    .block(timeout);
        } catch (Exception e) {
            log.warn("[AuthClient] Failed to fetch claims: {}", e.toString());
            return null;
        }
    }

    public UUID getUserIdByEmail(String email) {
        try {
            String idStr = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(userIdPath)
                            .queryParam("correoElectronico", email)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(timeout);

            if (idStr != null && !idStr.isBlank()) {
                idStr = idStr.replace("\"", "").trim();
                return UUID.fromString(idStr);
            }
            return null;
        } catch (Exception e) {
            log.error("[AuthClient] Error fetching userId by email: {}", e.toString());
            return null;
        }
    }

    public List<AuthUserDto> getUsuarios(String token) {
        try {
            return webClient.get()
                    .uri(usuariosPath)
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AuthUserDto>>() {})
                    .block(timeout);
        } catch (Exception e) {
            log.error("[AuthClient] Error fetching usuarios: {}", e.toString());
            return Collections.emptyList();
        }
    }

    public AuthUserDto getUsuarioById(UUID userId) {
        try {
            return webClient.get()
                    .uri(usuarioPath + "/" + userId.toString())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(AuthUserDto.class)
                    .block(timeout);
        } catch (Exception e) {
            log.error("[AuthClient] Error fetching usuario by id {}: {}", userId, e.toString());
            return null;
        }
    }

    public AuthBackupDto getBackup(String token) {
        try {
            return backupWebClient.get()
                    .uri(backupPath)
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(AuthBackupDto.class)
                    .block(timeout);
        } catch (Exception e) {
            log.error("[AuthClient] Error fetching backup: {}", e.toString());
            String errorMsg = "Error al obtener datos de backup del servicio de autenticación";
            if (e.getMessage() != null && e.getMessage().contains("DecodingException")) {
                errorMsg += ". Error de deserialización JSON - posible incompatibilidad en el formato del campo 'personalizacion'";
            } else if (e.getMessage() != null && e.getMessage().contains("MismatchedInputException")) {
                errorMsg += ". Error de deserialización - tipo de dato incompatible en la respuesta";
            }
            throw new RuntimeException(errorMsg, e);
        }
    }

    public Map<String, Integer> restaurarBackup(AuthBackupDto backupDto) {
        try {
            return backupWebClient.post()
                    .uri("/backup/restaurar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(backupDto))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Integer>>() {})
                    .block(timeout);
        } catch (Exception e) {
            log.error("[AuthClient] Error restaurando backup: {}", e.toString());
            throw new RuntimeException("Error al restaurar backup en el servicio de autenticación: " + e.getMessage(), e);
        }
    }

    public Boolean isExpired(String token) {
        try {
            return webClient.post()
                    .uri(isExpiredPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of("token", token)))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block(timeout);
        } catch (Exception e) {
            log.error("[AuthClient] Error checking token expiration: {}", e.toString());
            return false;
        }
    }
}
