package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProveedorNoDisponibleException extends BusinessException {
    public ProveedorNoDisponibleException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.SERVICE_UNAVAILABLE; }
    @Override public String getCodigo()     { return "PROVEEDOR_NO_DISPONIBLE"; }
}
