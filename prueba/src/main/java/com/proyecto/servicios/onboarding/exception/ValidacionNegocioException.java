package com.proyecto.servicios.onboarding.exception;

import org.springframework.http.HttpStatus;

public class ValidacionNegocioException extends BusinessException {
    private final String codigo;
    private final HttpStatus status;
    public ValidacionNegocioException(String codigo, String msg) {
        this(codigo, msg, HttpStatus.BAD_REQUEST);
    }
    public ValidacionNegocioException(String codigo, String msg, HttpStatus status) {
        super(msg);
        this.codigo = codigo;
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
    public String getCodigo()     { return codigo; }

    public static ValidacionNegocioException edadInvalida()       { return new ValidacionNegocioException("EDAD_INVALIDA", "El cliente debe ser mayor de edad"); }
    public static ValidacionNegocioException passwordInvalido()   { return new ValidacionNegocioException("PASSWORD_INVALIDO", "La contrasenia no cumple la politica de seguridad"); }
    public static ValidacionNegocioException saldoInvalido()      { return new ValidacionNegocioException("SALDO_INVALIDO", "El saldo no puede ser negativo"); }
    public static ValidacionNegocioException clienteInactivo()    { return new ValidacionNegocioException("CLIENTE_INACTIVO", "El cliente esta inactivo y no puede tener cuentas activas", HttpStatus.CONFLICT); }
    public static ValidacionNegocioException campoInmutable(String c) { return new ValidacionNegocioException("CAMPO_INMUTABLE", "El campo '" + c + "' no puede modificarse"); }
}
