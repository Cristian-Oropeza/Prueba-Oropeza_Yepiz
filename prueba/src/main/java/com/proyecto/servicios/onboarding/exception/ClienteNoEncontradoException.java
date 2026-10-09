package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

public class ClienteNoEncontradoException extends BusinessException {
    public ClienteNoEncontradoException(String msg) { super(msg); }
    public static ClienteNoEncontradoException porId(Long id)     { return new ClienteNoEncontradoException("Cliente no encontrado con id " + id); }
    public static ClienteNoEncontradoException porCurp(String v)  { return new ClienteNoEncontradoException("Cliente no encontrado con CURP " + v); }
    public static ClienteNoEncontradoException porRfc(String v)   { return new ClienteNoEncontradoException("Cliente no encontrado con RFC " + v); }
    public static ClienteNoEncontradoException porCorreo(String v){ return new ClienteNoEncontradoException("Cliente no encontrado con correo " + v); }
    public HttpStatus getStatus() { return HttpStatus.NOT_FOUND; }
    public String getCodigo()     { return "CLIENTE_NO_ENCONTRADO"; }
}
