package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CuentaNoPerteneceAlClienteException extends BusinessException {
    public CuentaNoPerteneceAlClienteException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.FORBIDDEN; }
    @Override public String getCodigo()     { return "CUENTA_NO_PERTENECE"; }
}
