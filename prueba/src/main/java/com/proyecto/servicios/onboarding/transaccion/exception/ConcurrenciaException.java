package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Dos operaciones simultaneas chocaron en el saldo tras N reintentos optimistas. Se devuelve 409
 * para que el cliente reintente desde su lado.
 */
public class ConcurrenciaException extends BusinessException {
    public ConcurrenciaException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.CONFLICT; }
    @Override public String getCodigo()     { return "CONCURRENCIA"; }
}
