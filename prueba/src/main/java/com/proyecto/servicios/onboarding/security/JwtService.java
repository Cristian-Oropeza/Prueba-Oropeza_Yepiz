package com.proyecto.servicios.onboarding.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    /** Default seguro: si falla la resolucion de la env var, se usa este literal. */
    private static final String SECRET_FALLBACK =
            "default-dev-secret-no-usar-en-produccion-nunca-32bytes-1234567890";

    @Value("${app.security.jwt.secret:}")
    private String secret;
    @Value("${app.security.jwt.expiration-ms:3600000}")
    private long expirationMs;
    @Value("${app.security.jwt.issuer:onboarding-clientes}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    void init() {
        // Defensa: si el secret vino vacio o quedo con ${...} literal (placeholder mal escrito),
        //         usamos el fallback para no tumbar la app en arranque.
        if (secret == null || secret.isBlank() || secret.contains("${")) {
            log.warn("JWT secret ausente o mal formado ('{}'); usando fallback de desarrollo. " +
                    "Configura JWT_SECRET en el entorno para produccion.", secret);
            secret = SECRET_FALLBACK;
        }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            log.warn("JWT secret muy corto ({} bytes). Recomendado >= 32. Se padea a 32.", bytes.length);
        }
        this.key = Keys.hmacShaKeyFor(padTo32(bytes));
        log.info("JwtService inicializado (longitud secret: {} bytes, exp: {} s)",
                Math.max(bytes.length, 32), expirationMs / 1000);
    }

    private byte[] padTo32(byte[] input) {
        if (input.length >= 32) return input;
        byte[] out = new byte[32];
        System.arraycopy(input, 0, out, 0, input.length);
        return out;
    }

    public String generarToken(Long clienteId, String correo) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(correo)
                .issuer(issuer)
                .claims(Map.of("clienteId", clienteId))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims validar(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getExpirationSeconds() { return expirationMs / 1000L; }
}
