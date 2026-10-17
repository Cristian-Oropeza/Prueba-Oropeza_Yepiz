package com.proyecto.servicios.onboarding.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Consulta unificada de clientes. Todos los filtros son opcionales y se
 * combinan con AND. Las validaciones se declaran aquí; el handler global
 * convierte las violaciones en GenericResponse sin controller lógico.
 */
@Getter @Setter @NoArgsConstructor
public class ClienteConsultaRequest {

    @Positive(message = "El id debe ser positivo")
    private Long id;

    @Size(max = 18, message = "La CURP no puede exceder 18 caracteres")
    private String curp;

    @Size(max = 13, message = "El RFC no puede exceder 13 caracteres")
    private String rfc;

    @Email(message = "El correo no tiene formato valido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String correo;

    @Pattern(regexp = "^$|^\\d{18}$", message = "El numero de cuenta debe tener 18 digitos")
    private String numeroCuenta;

    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 50, message = "El apellido paterno no puede exceder 50 caracteres")
    private String apellidoPaterno;

    @Size(max = 50, message = "El apellido materno no puede exceder 50 caracteres")
    private String apellidoMaterno;

    private Boolean activo;

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    @Valid
    private PaginacionRequest paginacion = new PaginacionRequest();
}
