package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

public class CredencialesInvalidasException extends BusinessException {
    public CredencialesInvalidasException() { super("Credenciales invalidas"); }
    public HttpStatus getStatus() { return HttpStatus.UNAUTHORIZED; }
    public String getCodigo()     { return "CREDENCIALES_INVALIDAS"; }
}
