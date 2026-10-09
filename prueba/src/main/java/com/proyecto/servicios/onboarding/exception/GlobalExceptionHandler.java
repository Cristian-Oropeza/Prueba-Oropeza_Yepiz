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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler global de excepciones. SIEMPRE devuelve un {@link GenericResponse}
 *   { "codigo": <http status>, "mensaje": "...", "detalles": [opcional] }
 *
 * Alcance: solo el modulo onboarding. El codigo legacy GestoPago tiene su
 * propio handler (si llega a activarse con gestopago.enabled=true).
 */
@RestControllerAdvice(basePackages = "com.proyecto.servicios.onboarding")
@Slf4j
public class GlobalExceptionHandler {

    // ========== Excepciones de negocio (CURP duplicada, cliente no encontrado, etc.) ==========
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GenericResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("Business: {} {} - {}", ex.getStatus(), ex.getCodigo(), ex.getMessage());
        return respond(ex.getStatus(), ex.getMessage());
    }

    // ========== Bean Validation en @RequestBody (@Valid) ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        // Aplanamos nombres: "domicilio.codigoPostal" -> "codigoPostal"
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> aplanarCampo(fe.getField()) + ": "
                        + (fe.getDefaultMessage() == null ? "invalido" : fe.getDefaultMessage()))
                .distinct()
                .collect(Collectors.toList());
        return respond(HttpStatus.BAD_REQUEST, "Uno o mas campos son invalidos", detalles);
    }

    // ========== Bean Validation en @RequestParam / @PathVariable ==========
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
        return respond(HttpStatus.BAD_REQUEST, "Parametros invalidos", detalles);
    }

    // ========== JSON mal formado o enum invalido (sexo="HOMBRE", etc.) ==========
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
                String msg = campo + ": valor '" + valor + "' invalido. Valores permitidos: [" + permitidos + "]";
                return respond(HttpStatus.BAD_REQUEST, "Valor de campo invalido", List.of(msg));
            }
            String tipo = target == null ? "tipo esperado" : target.getSimpleName();
            String msg = campo + ": no se pudo convertir '" + valor + "' al tipo " + tipo;
            return respond(HttpStatus.BAD_REQUEST, "Valor de campo invalido", List.of(msg));
        }
        return respond(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion es invalido o no se pudo parsear");
    }

    // ========== Query/path params con tipo incorrecto (?clienteId=abc) ==========
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericResponse> handleMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String tipo = ex.getRequiredType() == null ? "" : ex.getRequiredType().getSimpleName();
        String msg = ex.getName() + ": valor '" + ex.getValue() + "' invalido"
                + (tipo.isEmpty() ? "" : " para tipo " + tipo);
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String permitidos = Arrays.stream(ex.getRequiredType().getEnumConstants())
                    .map(Object::toString).collect(Collectors.joining(", "));
            msg = msg + ". Valores permitidos: [" + permitidos + "]";
        }
        return respond(HttpStatus.BAD_REQUEST, "Parametro invalido", List.of(msg));
    }

    // ========== Violaciones de integridad en BD (unique, check, fk) ==========
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String raw = ex.getMostSpecificCause().getMessage();
        String lower = raw == null ? "" : raw.toLowerCase();
        HttpStatus status = HttpStatus.CONFLICT;
        String friendly = "Violacion de restricciones de integridad en base de datos";

        if (lower.contains("uk_clientes_curp"))                 { friendly = "Ya existe un cliente con esa CURP"; }
        else if (lower.contains("uk_clientes_rfc"))             { friendly = "Ya existe un cliente con ese RFC"; }
        else if (lower.contains("uk_clientes_correo"))          { friendly = "Ya existe un cliente con ese correo"; }
        else if (lower.contains("uk_usuarios_correo"))          { friendly = "Ya existe un usuario con ese correo"; }
        else if (lower.contains("uk_cuentas_numero"))           { friendly = "Numero de cuenta duplicado"; }
        else if (lower.contains("ck_clientes_curp_formato"))    { friendly = "CURP con formato invalido"; status = HttpStatus.BAD_REQUEST; }
        else if (lower.contains("ck_clientes_rfc_formato"))     { friendly = "RFC con formato invalido";  status = HttpStatus.BAD_REQUEST; }
        else if (lower.contains("ck_clientes_telefono"))        { friendly = "Telefono con formato invalido"; status = HttpStatus.BAD_REQUEST; }
        else if (lower.contains("ck_cuentas_saldo_no_negativo")){ friendly = "El saldo no puede ser negativo"; status = HttpStatus.BAD_REQUEST; }

        log.warn("Integrity: {} - {}", status, raw);
        return respond(status, friendly);
    }

    // ========== Catch-all ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleAny(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado");
    }

    // ========== Helpers ==========
    private String aplanarCampo(String field) {
        if (field == null) return "campo";
        int lastDot = field.lastIndexOf('.');
        return lastDot < 0 ? field : field.substring(lastDot + 1);
    }

    private ResponseEntity<GenericResponse> respond(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(GenericResponse.of(status.value(), mensaje));
    }

    private ResponseEntity<GenericResponse> respond(HttpStatus status, String mensaje, List<String> detalles) {
        return ResponseEntity.status(status).body(GenericResponse.of(status.value(), mensaje, detalles));
    }
}
