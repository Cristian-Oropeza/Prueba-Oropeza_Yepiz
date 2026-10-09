package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordSeguraValidator implements ConstraintValidator<PasswordSegura, String> {

    // >=8 chars, >=1 mayuscula, >=1 minuscula, >=1 digito, >=1 especial
    private static final Pattern PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return false;
        return PATTERN.matcher(value).matches();
    }
}
