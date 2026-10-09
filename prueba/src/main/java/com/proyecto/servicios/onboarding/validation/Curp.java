package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * CURP valida: 18 chars + formato oficial + digito verificador correcto (algoritmo RENAPO).
 */
@Documented
@Constraint(validatedBy = CurpValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Curp {
    String message() default "CURP invalida (formato o digito verificador incorrecto)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
