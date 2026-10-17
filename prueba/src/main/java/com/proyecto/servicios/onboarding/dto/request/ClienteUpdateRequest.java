package com.proyecto.servicios.onboarding.dto.request;

import com.proyecto.servicios.onboarding.entity.enums.EstadoCivil;
import com.proyecto.servicios.onboarding.entity.enums.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * PATCH-style: todos los campos opcionales excepto el id. CURP, RFC y
 * numero_cuenta son INMUTABLES (ignorados aunque se envien).
 */
@Getter @Setter @NoArgsConstructor
public class ClienteUpdateRequest {

    @NotNull(message = "El id del cliente es obligatorio")
    @Positive(message = "El id debe ser positivo")
    private Long id;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$",
            message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]*$",
            message = "El segundo nombre solo puede contener letras y espacios")
    private String segundoNombre;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$",
            message = "El apellido paterno solo puede contener letras y espacios")
    private String apellidoPaterno;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$",
            message = "El apellido materno solo puede contener letras y espacios")
    private String apellidoMaterno;

    private Sexo sexo;

    @Size(min = 2, max = 60)
    private String nacionalidad;

    private EstadoCivil estadoCivil;

    @Email(message = "El correo no tiene formato valido")
    @Size(max = 100)
    private String correo;

    @Pattern(regexp = "^\\d{10}$", message = "El telefono movil debe tener 10 digitos")
    private String telefonoMovil;

    @Pattern(regexp = "^\\d{10}$", message = "El telefono alterno debe tener 10 digitos")
    private String telefonoAlterno;

    @Size(min = 2, max = 80)
    private String ocupacion;

    @Size(min = 2, max = 100)
    private String empresa;

    @DecimalMin(value = "0.01", message = "El ingreso mensual debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal ingresoMensual;

    @Valid
    private DomicilioRequest domicilio;
}
