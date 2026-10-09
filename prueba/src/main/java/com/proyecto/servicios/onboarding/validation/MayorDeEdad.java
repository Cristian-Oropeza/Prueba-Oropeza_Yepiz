package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MayorDeEdadValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MayorDeEdad {
    int value() default 18;
    String message() default "El cliente debe ser mayor de edad ({value} anios)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
