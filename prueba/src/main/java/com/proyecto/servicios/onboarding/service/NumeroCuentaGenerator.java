package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera un numero de cuenta de 18 digitos estilo CLABE:
 *   prefijo(6) + secuencia/aleatorio(12) => 18 digitos.
 * Reintenta hasta 5 veces ante colision (probabilidad practicamente cero, pero blindado).
 */
@Component
@RequiredArgsConstructor
public class NumeroCuentaGenerator {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int MAX_INTENTOS = 5;

    private final CuentaRepository cuentaRepository;

    @Value("${app.cuenta.prefijo-clabe:014180}")
    private String prefijo;

    public String generar() {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            String candidato = prefijo + random12();
            if (!cuentaRepository.existsByNumeroCuenta(candidato)) return candidato;
        }
        throw new IllegalStateException("No se pudo generar un numero de cuenta unico tras " + MAX_INTENTOS + " intentos");
    }

    private String random12() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }
}
