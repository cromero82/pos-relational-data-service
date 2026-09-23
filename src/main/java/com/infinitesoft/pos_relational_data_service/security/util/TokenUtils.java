package com.infinitesoft.pos_relational_data_service.security.util;

import javax.servlet.http.HttpServletRequest;

public final class TokenUtils {

    private TokenUtils() {}

    public static String extractToken(HttpServletRequest request) {
        if (request == null) return null;
        // Try standard Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            authHeader = request.getHeader("authorization");
        }
        if (authHeader == null) {
            var names = request.getHeaderNames();
            while (names != null && names.hasMoreElements()) {
                String name = names.nextElement();
                if ("authorization".equalsIgnoreCase(name)) {
                    authHeader = request.getHeader(name);
                    break;
                }
            }
        }

        String rawToken = null;
        if (authHeader != null && !authHeader.isBlank()) {
            String prefix = "Bearer ";
            if (authHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
                rawToken = authHeader.substring(prefix.length()).trim();
            } else {
                rawToken = authHeader.trim();
            }
        }

        if (rawToken == null || rawToken.isBlank()) {
            rawToken = request.getHeader("token");
        }
        if (rawToken == null || rawToken.isBlank()) {
            rawToken = request.getParameter("token");
        }
        return rawToken;
    }
}
