package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TransaccionDuplicadaException extends BusinessException {
    public TransaccionDuplicadaException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.CONFLICT; }
    @Override public String getCodigo()     { return "TRANSACCION_DUPLICADA"; }
}
