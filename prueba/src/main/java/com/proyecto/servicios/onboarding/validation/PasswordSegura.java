package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordSeguraValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordSegura {
    String message() default "La contrasenia debe tener minimo 8 caracteres, con al menos una mayuscula, una minuscula, un numero y un caracter especial";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
