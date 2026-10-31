package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MontoFueraDeRangoException extends BusinessException {
    public MontoFueraDeRangoException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.UNPROCESSABLE_ENTITY; }
    @Override public String getCodigo()     { return "MONTO_FUERA_DE_RANGO"; }
}
