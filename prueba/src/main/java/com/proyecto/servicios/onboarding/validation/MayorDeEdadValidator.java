package com.proyecto.servicios.onboarding.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {

    private int edadMinima;

    @Override
    public void initialize(MayorDeEdad ann) {
        this.edadMinima = ann.value();
    }

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext ctx) {
        if (fechaNacimiento == null) return true; // @NotNull se encarga
        if (fechaNacimiento.isAfter(LocalDate.now())) return false;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= edadMinima;
    }
}
