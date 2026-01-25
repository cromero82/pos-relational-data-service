package com.infinitesoft.pos_relational_data_service.security.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.security.client.AuthClient;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthRoleDto;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.service.AuthValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthValidationServiceImpl implements AuthValidationService {

    private final AuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(AuthValidationServiceImpl.class);

    public AuthValidationServiceImpl(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public AuthUserDto validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            // 1) Validate token
            Boolean ok = authClient.validateToken(token);
            if (ok != null && ok) {
                // 2) If valid, fetch claims/user info
                AuthUserDto user = authClient.getClaims(token);
                if (user != null) {
                    return user;
                }
                // 3) Fallback: Build minimal user from JWT payload claims
                return parseUserFromJwt(token);
            }
            return null;
        } catch (Exception ex) {
            log.error("[AuthValidation] Error validating token: {}", ex.toString(), ex);
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
    public UUID fetchUserIdByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return authClient.getUserIdByEmail(email);
    }

    @Override
    public AuthUserDto fetchUserInfoById(UUID userId) {
        if (userId == null) return null;
        return authClient.getUsuarioById(userId);
    }
}
