package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

public class DuplicadoException extends BusinessException {
    private final String codigo;
    public DuplicadoException(String codigo, String msg) { super(msg); this.codigo = codigo; }
    public static DuplicadoException curp(String v)   { return new DuplicadoException("CURP_DUPLICADA",   "Ya existe un cliente con CURP "   + v); }
    public static DuplicadoException rfc(String v)    { return new DuplicadoException("RFC_DUPLICADO",    "Ya existe un cliente con RFC "    + v); }
    public static DuplicadoException correo(String v) { return new DuplicadoException("CORREO_DUPLICADO", "Ya existe un cliente/usuario con correo " + v); }
    public HttpStatus getStatus() { return HttpStatus.CONFLICT; }
    public String getCodigo()     { return codigo; }
}
