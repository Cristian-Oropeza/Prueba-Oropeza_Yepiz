package com.proyecto.servicios.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final String codigo;
    private final String mensaje;
}
