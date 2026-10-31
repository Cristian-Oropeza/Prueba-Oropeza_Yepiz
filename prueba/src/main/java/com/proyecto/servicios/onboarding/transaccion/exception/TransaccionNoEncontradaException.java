package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TransaccionNoEncontradaException extends BusinessException {
    public TransaccionNoEncontradaException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.NOT_FOUND; }
    @Override public String getCodigo()     { return "TRANSACCION_NO_ENCONTRADA"; }
}
