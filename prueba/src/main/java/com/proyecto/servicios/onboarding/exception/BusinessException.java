package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

/** Excepcion base de negocio. Cada subclase define status y codigo. */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String msg) { super(msg); }
    public abstract HttpStatus getStatus();
    public abstract String getCodigo();
}
