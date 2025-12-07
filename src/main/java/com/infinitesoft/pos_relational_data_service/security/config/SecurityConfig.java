package com.infinitesoft.pos_relational_data_service.security.config;

import com.infinitesoft.pos_relational_data_service.security.filter.TokenAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenAuthFilter tokenAuthFilter;

    public SecurityConfig(TokenAuthFilter tokenAuthFilter) {
        this.tokenAuthFilter = tokenAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/_debug/**", "/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins:http://localhost:4200}") String allowedOriginsProp,
            @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") String allowedMethodsProp,
            @Value("${cors.allowed-headers:Authorization,authorization,Content-Type,Accept,X-Requested-With,token}") String allowedHeadersProp,
            @Value("${cors.exposed-headers:Authorization,authorization}") String exposedHeadersProp,
            @Value("${cors.allow-credentials:true}") boolean allowCredentials
    ) {
        CorsConfiguration config = new CorsConfiguration();
        for (String o : allowedOriginsProp.split(",")) {
            config.addAllowedOrigin(o.trim());
        }
        for (String m : allowedMethodsProp.split(",")) {
            config.addAllowedMethod(m.trim());
        }
        for (String h : allowedHeadersProp.split(",")) {
            config.addAllowedHeader(h.trim());
        }
        for (String h : exposedHeadersProp.split(",")) {
            config.addExposedHeader(h.trim());
        }
        config.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
