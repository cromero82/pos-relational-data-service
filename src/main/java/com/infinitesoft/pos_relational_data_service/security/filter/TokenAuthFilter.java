package com.infinitesoft.pos_relational_data_service.security.filter;

import com.infinitesoft.pos_relational_data_service.security.dto.AuthRoleDto;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.service.AuthValidationService;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final AuthValidationService authValidationService;
    private static final Logger log = LoggerFactory.getLogger(TokenAuthFilter.class);

    public TokenAuthFilter(AuthValidationService authValidationService) {
        this.authValidationService = authValidationService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Skip auth for actuator health/info and internal debug endpoints (handled by Spring Security config)
        String uri = request.getRequestURI();
        if (uri != null) {
            String u = uri.toLowerCase();
            if (u.equals("/actuator/health") || u.startsWith("/actuator/health/")
                    || u.equals("/actuator/info") || u.startsWith("/actuator/info/")
                    || u.startsWith("/_debug/")) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Allow CORS preflight to pass through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Try standard Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            authHeader = request.getHeader("authorization");
        }
        if (authHeader == null) {
            // Last resort: enumerate headers to find any variant of Authorization
            var names = request.getHeaderNames();
            while (names != null && names.hasMoreElements()) {
                String name = names.nextElement();
                if ("authorization".equalsIgnoreCase(name)) {
                    authHeader = request.getHeader(name);
                    break;
                }
            }
            if (authHeader == null) {
                // Log all headers to diagnose missing Authorization
                StringBuilder all = new StringBuilder();
                var hn = request.getHeaderNames();
                while (hn != null && hn.hasMoreElements()) {
                    String n = hn.nextElement();
                    all.append(n).append("=").append(request.getHeader(n)).append("; ");
                }
                log.info("[AuthFilter] Authorization header NOT FOUND. Method={} URI={} Headers=[{}]",
                        request.getMethod(), request.getRequestURI(), all.toString());
            }
        }

        String rawToken = null;
        if (authHeader != null && !authHeader.isBlank()) {
            String prefix = "Bearer ";
            if (authHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
                rawToken = authHeader.substring(prefix.length()).trim();
            } else {
                // If Authorization is present but without Bearer, try using it as-is
                rawToken = authHeader.trim();
            }
        }

        // Fallback to custom header 'token'
        if (rawToken == null || rawToken.isBlank()) {
            rawToken = request.getHeader("token");
        }
        // Fallback to query param ?token=
        if ((rawToken == null || rawToken.isBlank())) {
            rawToken = request.getParameter("token");
        }

        if (rawToken == null || rawToken.isBlank()) {
            // Log all headers again for diagnosis
            StringBuilder all = new StringBuilder();
            var hn = request.getHeaderNames();
            while (hn != null && hn.hasMoreElements()) {
                String n = hn.nextElement();
                all.append(n).append("=").append(request.getHeader(n)).append("; ");
            }
            log.info("[AuthFilter] Missing token. Method={} URI={} Headers=[{}]", request.getMethod(), request.getRequestURI(), all.toString());
            log.debug("[AuthFilter] Missing token: no Authorization/token header present");
            unauthorized(response, "Missing token: use Authorization: Bearer <token> or token header");
            return;
        }

        AuthUserDto user = authValidationService.validateToken(rawToken);
        if (user == null) {
            log.debug("[AuthFilter] Token validation failed or service unavailable");
            unauthorized(response, "Invalid or expired token");
            return;
        }

        // Fetch user ID if missing
        if (user.getId() == null && user.getCorreoElectronico() != null) {
            try {
                UUID userId = authValidationService.fetchUserIdByEmail(user.getCorreoElectronico());
                user.setId(userId);
            } catch (Exception e) {
                log.warn("[AuthFilter] Could not fetch user ID for email {}: {}", user.getCorreoElectronico(), e.getMessage());
            }
        }

        List<GrantedAuthority> authorities = Objects.requireNonNullElse(user.getRoles(), List.<AuthRoleDto>of())
                .stream()
                .map(AuthRoleDto::getSigla)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(sigla -> new SimpleGrantedAuthority("ROLE_" + sigla))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, // Principal is now the full AuthUserDto
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"error\":\"unauthorized\",\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
        response.getWriter().write(body);
        response.getWriter().flush();
    }
}
