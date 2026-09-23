package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GestoPagoExceptionHandler {

    @ExceptionHandler(GestoPagoIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrationException(GestoPagoIntegrationException e) {
        GestoPagoErrorCode errorCode = e.getErrorCode();
        log.warn("Peticion finalizada con error de integracion GestoPago: codigo={}", errorCode.getCode());
        return ResponseEntity
                .status(resolveStatus(errorCode))
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    private HttpStatus resolveStatus(GestoPagoErrorCode errorCode) {
        return switch (errorCode) {
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }
}
