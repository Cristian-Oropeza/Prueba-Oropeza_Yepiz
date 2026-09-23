package com.proyecto.servicios.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GestoPagoErrorCode {

    AUTHENTICATION_ERROR("GP-001", "Error de autenticacion con GestoPago"),
    CLIENT_ERROR("GP-002", "GestoPago rechazo la peticion"),
    SERVICE_UNAVAILABLE("GP-003", "GestoPago no esta disponible"),
    TIMEOUT("GP-004", "Tiempo de espera agotado al consultar GestoPago"),
    COMMUNICATION_ERROR("GP-005", "Error de comunicacion con GestoPago"),
    INVALID_RESPONSE("GP-006", "La respuesta de GestoPago no tiene un formato valido"),
    BUSINESS_ERROR("GP-007", "GestoPago respondio con un codigo de error"),
    EMPTY_CATALOG("GP-008", "GestoPago respondio sin productos");

    private final String code;
    private final String message;
}