package com.proyecto.servicios.onboarding.dto.request;

import com.proyecto.servicios.onboarding.entity.enums.EstadoCivil;
import com.proyecto.servicios.onboarding.entity.enums.Sexo;
import com.proyecto.servicios.onboarding.validation.Curp;
import com.proyecto.servicios.onboarding.validation.MayorDeEdad;
import com.proyecto.servicios.onboarding.validation.PasswordSegura;
import com.proyecto.servicios.onboarding.validation.Rfc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteAltaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$", message = "El nombre solo puede contener letras y espacios")
        String nombre,

        @Size(max = 50, message = "El segundo nombre no debe exceder 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]*$", message = "El segundo nombre solo puede contener letras y espacios")
        String segundoNombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$", message = "El apellido paterno solo puede contener letras y espacios")
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$", message = "El apellido materno solo puede contener letras y espacios")
        String apellidoMaterno,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento no puede ser futura")
        @MayorDeEdad(value = 18, message = "El cliente debe ser mayor de edad (18 anios)")
        LocalDate fechaNacimiento,

        @NotBlank(message = "La CURP es obligatoria")
        @Curp
        String curp,

        @NotBlank(message = "El RFC es obligatorio")
        @Rfc
        String rfc,

        @NotNull(message = "El sexo es obligatorio (MASCULINO, FEMENINO u OTRO)")
        Sexo sexo,

        @NotBlank(message = "La nacionalidad es obligatoria")
        @Size(min = 2, max = 60, message = "La nacionalidad debe tener entre 2 y 60 caracteres")
        String nacionalidad,

        @NotNull(message = "El estado civil es obligatorio")
        EstadoCivil estadoCivil,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
        String correo,

        @NotBlank(message = "El telefono movil es obligatorio")
        @Pattern(regexp = "^\\d{10}$", message = "El telefono movil debe contener exactamente 10 digitos numericos")
        String telefonoMovil,

        @Pattern(regexp = "^\\d{10}$", message = "El telefono alterno debe contener exactamente 10 digitos numericos")
        String telefonoAlterno,

        @NotBlank(message = "La ocupacion es obligatoria")
        @Size(min = 2, max = 80, message = "La ocupacion debe tener entre 2 y 80 caracteres")
        String ocupacion,

        @NotBlank(message = "La empresa es obligatoria")
        @Size(min = 2, max = 100, message = "La empresa debe tener entre 2 y 100 caracteres")
        String empresa,

        @NotNull(message = "El ingreso mensual es obligatorio")
        @DecimalMin(value = "0.01", message = "El ingreso mensual debe ser mayor a cero")
        @Digits(integer = 10, fraction = 2, message = "El ingreso mensual debe tener a lo mas 10 enteros y 2 decimales")
        BigDecimal ingresoMensual,

        @NotBlank(message = "La contrasenia es obligatoria")
        @PasswordSegura
        String password,

        @NotNull(message = "El domicilio es obligatorio")
        @Valid DomicilioRequest domicilio

) {}
