package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

public class CuentaNoEncontradaException extends BusinessException {
    public CuentaNoEncontradaException(String msg) { super(msg); }
    public static CuentaNoEncontradaException porNumero(String n) { return new CuentaNoEncontradaException("Cuenta no encontrada: " + n); }
    public HttpStatus getStatus() { return HttpStatus.NOT_FOUND; }
    public String getCodigo()     { return "CUENTA_NO_ENCONTRADA"; }
}
