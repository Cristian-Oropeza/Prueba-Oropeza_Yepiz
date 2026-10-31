package com.proyecto.servicios.onboarding.transaccion.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class MontoValidoValidator implements ConstraintValidator<MontoValido, BigDecimal> {
    @Override
    public boolean isValid(BigDecimal valor, ConstraintValidatorContext ctx) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0 && valor.scale() <= 2;
    }
}
