package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.regex.Pattern;

/**
 * Valida RFC con mas rigor que un regex solo:
 *   - Longitud 12 (moral) o 13 (fisica)
 *   - Patron: 3-4 letras + 6 digitos (YYMMDD) + 3 chars homoclave
 *   - La fecha YYMMDD existe realmente (no 310232)
 *   - No permite fecha futura
 */
public class RfcValidator implements ConstraintValidator<Rfc, String> {

    private static final Pattern FORMATO_FISICA = Pattern.compile("^[A-ZÑ&]{4}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern FORMATO_MORAL  = Pattern.compile("^[A-ZÑ&]{3}\\d{6}[A-Z0-9]{3}$");

    @Override
    public boolean isValid(String rfc, ConstraintValidatorContext ctx) {
        if (rfc == null || rfc.isBlank()) return false;
        String r = rfc.toUpperCase().trim();
        int len = r.length();
        if (len != 12 && len != 13) return false;

        boolean fisica = (len == 13);
        if (fisica && !FORMATO_FISICA.matcher(r).matches()) return false;
        if (!fisica && !FORMATO_MORAL.matcher(r).matches()) return false;

        // Extraer YYMMDD
        int offset = fisica ? 4 : 3;
        String fecha = r.substring(offset, offset + 6);
        return fechaValida(fecha);
    }

    private boolean fechaValida(String yymmdd) {
        try {
            int yy = Integer.parseInt(yymmdd.substring(0, 2));
            int mm = Integer.parseInt(yymmdd.substring(2, 4));
            int dd = Integer.parseInt(yymmdd.substring(4, 6));
            // SAT asume: YY 00-29 -> 2000+; 30-99 -> 1900+
            int anio = (yy <= 29) ? 2000 + yy : 1900 + yy;
            if (mm < 1 || mm > 12) return false;
            int maxDay = YearMonth.of(anio, mm).lengthOfMonth();
            if (dd < 1 || dd > maxDay) return false;
            LocalDate f = LocalDate.of(anio, mm, dd);
            return !f.isAfter(LocalDate.now());
        } catch (NumberFormatException | DateTimeException e) {
            return false;
        }
    }
}
