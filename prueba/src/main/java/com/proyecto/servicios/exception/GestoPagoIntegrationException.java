package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class GestoPagoIntegrationException extends RuntimeException {

    private final GestoPagoErrorCode errorCode;

    public GestoPagoIntegrationException(GestoPagoErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public GestoPagoIntegrationException(GestoPagoErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.errorCode = errorCode;
    }

    public GestoPagoIntegrationException(GestoPagoErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}