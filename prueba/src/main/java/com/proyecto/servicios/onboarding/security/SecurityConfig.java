package com.proyecto.servicios.onboarding.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.onboarding.dto.response.GenericResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.security.jwt.enabled:false}")
    private boolean jwtEnabled;

    /** BCrypt con strength 10: ~60ms por hash. Balance perf/seguridad. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Headers de seguridad (defense in depth)
            .headers(h -> h
                .contentTypeOptions(ct -> {})
                .frameOptions(f -> f.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            )

            // 401 / 403 -> GenericResponse para mantener contrato uniforme
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, authEx) -> escribirError(
                        res, HttpServletResponse.SC_UNAUTHORIZED, "Autenticacion requerida"))
                .accessDeniedHandler((req, res, deniedEx) -> escribirError(
                        res, HttpServletResponse.SC_FORBIDDEN, "No tiene permisos para este recurso"))
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        if (jwtEnabled) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/v1/auth/**",
                            "/swagger-ui/**", "/swagger-ui.html",
                            "/api-docs/**", "/v3/api-docs/**",
                            "/actuator/health",
                            "/error"
                    ).permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/clientes").permitAll()
                    .anyRequest().authenticated()
            );
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }
        return http.build();
    }

    private void escribirError(HttpServletResponse res, int status, String mensaje) throws java.io.IOException {
        GenericResponse body = GenericResponse.of(status, mensaje);
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
