package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CuentaNoActivaException extends BusinessException {
    public CuentaNoActivaException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.UNPROCESSABLE_ENTITY; }
    @Override public String getCodigo()     { return "CUENTA_NO_ACTIVA"; }
}
