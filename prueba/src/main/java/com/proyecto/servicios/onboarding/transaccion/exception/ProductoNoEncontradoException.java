package com.proyecto.servicios.onboarding.transaccion.exception;

import com.proyecto.servicios.onboarding.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProductoNoEncontradoException extends BusinessException {
    public ProductoNoEncontradoException(String msg) { super(msg); }
    @Override public HttpStatus getStatus() { return HttpStatus.NOT_FOUND; }
    @Override public String getCodigo()     { return "PRODUCTO_NO_ENCONTRADO"; }
}
