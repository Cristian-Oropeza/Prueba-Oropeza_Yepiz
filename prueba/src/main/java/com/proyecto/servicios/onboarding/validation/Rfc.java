package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * RFC mexicano valido: 12 (persona moral) o 13 (persona fisica) caracteres,
 * con fecha interna valida y homoclave en formato correcto.
 */
@Documented
@Constraint(validatedBy = RfcValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rfc {
    String message() default "RFC invalido (formato o fecha incorrecta)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
