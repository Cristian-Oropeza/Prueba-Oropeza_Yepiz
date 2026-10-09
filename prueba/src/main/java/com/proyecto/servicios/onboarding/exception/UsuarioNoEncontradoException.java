package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

public class UsuarioNoEncontradoException extends BusinessException {
    public UsuarioNoEncontradoException(String msg) { super(msg); }
    public HttpStatus getStatus() { return HttpStatus.NOT_FOUND; }
    public String getCodigo()     { return "USUARIO_NO_ENCONTRADO"; }
}
