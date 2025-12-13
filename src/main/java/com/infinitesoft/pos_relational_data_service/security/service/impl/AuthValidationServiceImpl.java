package com.infinitesoft.pos_relational_data_service.security.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthRoleDto;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.service.AuthValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AuthValidationServiceImpl implements AuthValidationService {

    private final WebClient webClient;
    private final String validatePath;
    private final String claimsPath;
    private final String userIdByEmailPath;
    private final Duration timeout;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(AuthValidationServiceImpl.class);

    public AuthValidationServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${auth.service.base-url:http://localhost:8081}") String baseUrl,
            @Value("${auth.service.validate-path:/auth/validate}") String validatePath,
            @Value("${auth.service.claims-path:/auth/claims}") String claimsPath,
            @Value("${auth.service.user-id-path:/auth/usuario-id}") String userIdByEmailPath,
            @Value("${auth.service.timeout-ms:5000}") long timeoutMs
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.validatePath = validatePath;
        this.claimsPath = claimsPath;
        this.userIdByEmailPath = userIdByEmailPath;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    @Override
    public AuthUserDto validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            // 1) Validate token: endpoint returns only boolean true/false
            Mono<Boolean> monoBool = webClient.post()
                    .uri(validatePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of("token", token)))
                    .retrieve()
                    .bodyToMono(Boolean.class);
            Boolean ok = monoBool.block(timeout);
            if (ok != null && ok) {
                // 2) If valid, fetch claims/user info
                try {
                    Mono<AuthUserDto> monoUser = webClient.post()
                            .uri(claimsPath)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(Map.of("token", token)))
                            .retrieve()
                            .bodyToMono(AuthUserDto.class);
                    AuthUserDto user = monoUser.block(timeout);
                    if (user != null) {
                        return user;
                    }
                } catch (Exception e) {
                    log.warn("[AuthValidation] Failed to fetch claims from {}: {}", claimsPath, e.toString());
                    // If claims endpoint not available, fallback to parse JWT locally
                }
                // 3) Fallback: Build minimal user from JWT payload claims
                return parseUserFromJwt(token);
            }
            return null;
        } catch (Exception ex) {
            log.error("[AuthValidation] Error validating token against {}: {}", validatePath, ex.toString(), ex);
            return null;
        }
    }

    private AuthUserDto parseUserFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>(){});

            AuthUserDto user = new AuthUserDto();
            // Common fields in provided example
            Object nombre = payload.get("nombre");
            if (nombre instanceof String) {
                user.setNombre((String) nombre);
            }

            Object sub = payload.get("sub");
            if (sub instanceof String) {
                user.setCorreoElectronico((String) sub);
            }

            Object telefono = payload.get("telefono");
            if (telefono instanceof String) {
                user.setTelefono((String) telefono);
            }

            // Extract roles: could be array of strings or array of objects with 'sigla'/'nombre'
            List<AuthRoleDto> rolesOut = new ArrayList<>();
            Object rolesObj = payload.get("roles");
            if (rolesObj instanceof List) {
                List<?> list = (List<?>) rolesObj;
                for (Object r : list) {
                    AuthRoleDto role = new AuthRoleDto();
                    if (r instanceof String) {
                        String rs = (String) r;
                        role.setSigla(rs);
                        role.setNombre(rs);
                    } else if (r instanceof Map) {
                        Map<?,?> rm = (Map<?,?>) r;
                        Object sigla = rm.get("sigla");
                        Object rnombre = rm.get("nombre");
                        if (sigla instanceof String) role.setSigla((String) sigla);
                        if (rnombre instanceof String) role.setNombre((String) rnombre);
                    }
                    if (role.getSigla() != null && !role.getSigla().isBlank()) {
                        rolesOut.add(role);
                    }
                }
            }
            user.setRoles(rolesOut);
            return user;
        } catch (Exception e) {
            log.warn("[AuthValidation] Failed to parse JWT locally: {}", e.toString());
            return null;
        }
    }

    @Override
    public java.util.UUID fetchUserIdByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        try {
            Mono<String> mono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(userIdByEmailPath)
                            .queryParam("correoElectronico", email)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class);
            String idStr = mono.block(timeout);
            if (idStr == null || idStr.isBlank()) return null;
            // Remove quotes if response is a JSON string value
            idStr = idStr.replace("\"", "").trim();
            return java.util.UUID.fromString(idStr);
        } catch (Exception e) {
            log.error("[AuthValidation] Error fetching userId by email from {}: {}", userIdByEmailPath, e.toString(), e);
            return null;
        }
    }
}
