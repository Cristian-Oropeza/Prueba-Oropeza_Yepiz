package com.proyecto.servicios.onboarding.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.proyecto.servicios.onboarding.dto.response.GenericResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejo global y centralizado de excepciones. Los controllers NO llevan
 * try-catch ni validaciones manuales; todo cae aquí y devolvemos siempre
 * GenericResponse con mensajes claros y sin exponer detalles internos.
 */
@RestControllerAdvice(basePackages = "com.proyecto.servicios.onboarding")
@Slf4j
public class GlobalExceptionHandler {

    // ========== Excepciones de negocio ==========
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GenericResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("Business: {} {} - {}", ex.getStatus(), ex.getCodigo(), ex.getMessage());
        return build(ex.getStatus(), ex.getMessage(), null);
    }

    // ========== Validaciones @Valid en @RequestBody ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> aplanar(fe.getField()) + ": "
                        + (fe.getDefaultMessage() == null ? "invalido" : fe.getDefaultMessage()))
                .distinct()
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Uno o mas campos son invalidos", detalles);
    }

    // ========== Validaciones a nivel path/query (quedan por retrocompatibilidad) ==========
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<String> detalles = ex.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    String campo = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return campo + ": " + v.getMessage();
                })
                .distinct()
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Parametros invalidos", detalles);
    }

    // ========== JSON mal formado / enum invalido ==========
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            Class<?> target = ife.getTargetType();
            String campo = ife.getPath().isEmpty()
                    ? "campo"
                    : ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            String valor = String.valueOf(ife.getValue());

            if (target != null && target.isEnum()) {
                String permitidos = Arrays.stream(target.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                return build(HttpStatus.BAD_REQUEST, "Valor de campo invalido",
                        List.of(campo + ": valor '" + valor + "' invalido. Valores permitidos: [" + permitidos + "]"));
            }
            String tipo = target == null ? "tipo esperado" : target.getSimpleName();
            return build(HttpStatus.BAD_REQUEST, "Valor de campo invalido",
                    List.of(campo + ": no se pudo convertir '" + valor + "' al tipo " + tipo));
        }
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion es invalido o no se pudo parsear", null);
    }

    // ========== Query/Path params con tipo incorrecto ==========
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericResponse> handleMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String tipo = ex.getRequiredType() == null ? "" : ex.getRequiredType().getSimpleName();
        String msg = ex.getName() + ": valor '" + ex.getValue() + "' invalido"
                + (tipo.isEmpty() ? "" : " para tipo " + tipo);
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String permitidos = Arrays.stream(ex.getRequiredType().getEnumConstants())
                    .map(Object::toString).collect(Collectors.joining(", "));
            msg += ". Valores permitidos: [" + permitidos + "]";
        }
        return build(HttpStatus.BAD_REQUEST, "Parametro invalido", List.of(msg));
    }

    // ========== Violaciones de integridad en BD ==========
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String raw = ex.getMostSpecificCause().getMessage();
        String lower = raw == null ? "" : raw.toLowerCase();
        HttpStatus status = HttpStatus.CONFLICT;
        String friendly = "Violacion de restricciones de integridad en base de datos";

        if (lower.contains("uk_clientes_curp"))                  friendly = "Ya existe un cliente con esa CURP";
        else if (lower.contains("uk_clientes_rfc"))              friendly = "Ya existe un cliente con ese RFC";
        else if (lower.contains("uk_clientes_correo"))           friendly = "Ya existe un cliente con ese correo";
        else if (lower.contains("uk_usuarios_correo"))           friendly = "Ya existe un usuario con ese correo";
        else if (lower.contains("uk_cuentas_numero"))            friendly = "Numero de cuenta duplicado";
        else if (lower.contains("ck_clientes_curp_formato"))     { friendly = "CURP con formato invalido"; status = HttpStatus.BAD_REQUEST; }
        else if (lower.contains("ck_clientes_rfc_formato"))      { friendly = "RFC con formato invalido";  status = HttpStatus.BAD_REQUEST; }
        else if (lower.contains("ck_clientes_telefono"))         { friendly = "Telefono con formato invalido"; status = HttpStatus.BAD_REQUEST; }
        else if (lower.contains("ck_cuentas_saldo_no_negativo")) { friendly = "El saldo no puede ser negativo"; status = HttpStatus.BAD_REQUEST; }

        log.warn("Integrity: {} - {}", status, raw);
        return build(status, friendly, null);
    }

    // ========== Catch-all (nunca exponemos traza al cliente) ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleAny(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado", null);
    }

    // ========== Helpers (un solo build, sin sobrecarga) ==========
    private String aplanar(String field) {
        if (field == null) return "campo";
        int lastDot = field.lastIndexOf('.');
        return lastDot < 0 ? field : field.substring(lastDot + 1);
    }

    private ResponseEntity<GenericResponse> build(HttpStatus status, String mensaje, List<String> detalles) {
        GenericResponse body = GenericResponse.builder()
                .codigo(status.value())
                .mensaje(mensaje)
                .detalles(detalles == null ? Collections.emptyList() : detalles)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
