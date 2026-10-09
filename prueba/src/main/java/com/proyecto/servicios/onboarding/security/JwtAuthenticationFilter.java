package com.proyecto.servicios.onboarding.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT: si viene el header `Authorization: Bearer <token>` y es valido,
 * lo pone en el SecurityContext. Si NO viene, deja pasar sin autenticar.
 *
 * El comportamiento "obligatorio vs opcional" lo controla SecurityConfig
 * (mediante permitAll() vs authenticated()).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader(HEADER);
        if (auth != null && auth.startsWith(PREFIX)) {
            String token = auth.substring(PREFIX.length()).trim();
            try {
                Claims claims = jwtService.validar(token);
                String correo = claims.getSubject();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                correo, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException ex) {
                log.debug("JWT invalido: {}", ex.getMessage());
                // No cortamos la request: si el endpoint requiere auth, Spring Security lo rechazara.
            }
        }
        chain.doFilter(req, res);
    }
}
