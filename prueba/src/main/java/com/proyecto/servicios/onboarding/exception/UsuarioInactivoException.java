package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

public class UsuarioInactivoException extends BusinessException {
    public UsuarioInactivoException() { super("Usuario inactivo"); }
    public HttpStatus getStatus() { return HttpStatus.UNAUTHORIZED; }
    public String getCodigo()     { return "USUARIO_INACTIVO"; }
}
