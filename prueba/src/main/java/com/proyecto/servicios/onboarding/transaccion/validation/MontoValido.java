package com.proyecto.servicios.onboarding.transaccion.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MontoValidoValidator.class)
public @interface MontoValido {
    String message() default "El monto debe ser positivo y tener a lo mas 2 decimales";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
