package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Valida CURP mexicana segun especificacion RENAPO:
 *   - 18 caracteres [A-Z0-9]
 *   - Posiciones y formato: ^[A-Z][AEIOUX][A-Z]{2}\d{6}[HMX][A-Z]{5}[A-Z0-9]\d$
 *   - Digito verificador (char 18) calculado con algoritmo oficial
 *
 * Nota: NO validamos palabras altisonantes ni concordancia con entidad federativa
 * (eso es responsabilidad de RENAPO, no de un onboarding). Lo del digito es la
 * garantia practica contra teclazos aleatorios.
 */
public class CurpValidator implements ConstraintValidator<Curp, String> {

    private static final Pattern FORMATO = Pattern.compile(
            "^[A-Z][AEIOUX][A-Z]{2}\\d{6}[HMX][A-Z]{5}[A-Z0-9]\\d$"
    );

    // Tabla oficial de valores para el digito verificador
    private static final String TABLA = "0123456789ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";

    @Override
    public boolean isValid(String curp, ConstraintValidatorContext ctx) {
        if (curp == null || curp.isBlank()) return false;
        String c = curp.toUpperCase().trim();
        if (c.length() != 18) return false;
        if (!FORMATO.matcher(c).matches()) return false;
        return digitoVerificadorValido(c);
    }

    private boolean digitoVerificadorValido(String curp) {
        int suma = 0;
        for (int i = 0; i < 17; i++) {
            int valor = TABLA.indexOf(curp.charAt(i));
            if (valor < 0) return false;
            suma += valor * (18 - i);
        }
        int digitoEsperado = (10 - (suma % 10)) % 10;
        int digitoReal = Character.digit(curp.charAt(17), 10);
        return digitoEsperado == digitoReal;
    }
}
